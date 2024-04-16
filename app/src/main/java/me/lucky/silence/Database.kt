package me.lucky.silence

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RenameTable
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber

@Database(
    entities = [AllowNumber::class],
    version = 3,
    autoMigrations = [AutoMigration(from = 1, to = 2, spec = AutoMigration1to2::class)],
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun allowNumberDao(): AllowNumberDao

    companion object {
        @Volatile private var instance: AppDatabase? = null
        private const val DATABASE_NAME = "app.db"

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room
                .databaseBuilder(context.applicationContext, AppDatabase::class.java, DATABASE_NAME)
                .addMigrations(MIGRATION_2_3)
                .allowMainThreadQueries()
                .build()
        }
    }
}

@RenameTable(fromTableName = "sms_filter", toTableName = "tmp_number")
private class AutoMigration1to2 : AutoMigrationSpec

private val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE tmp_number RENAME TO allow_number")
        db.execSQL("ALTER TABLE allow_number RENAME COLUMN ts_created TO ttl")
        db.execSQL(
            "UPDATE allow_number SET ttl = ttl + ${AllowNumberDao.INACTIVE_DURATION}")
    }
}

@Dao
interface AllowNumberDao {
    companion object {
        // outdated
        const val INACTIVE_DURATION = 48 * 60 * 60L
    }

    @Insert
    fun insert(obj: AllowNumber)

    @Query("UPDATE allow_number SET ttl = :ts WHERE phone_number = :phoneNumber")
    fun updateTimestamp(ts: Long, phoneNumber: String)

    @Query("DELETE FROM allow_number WHERE ttl < :ts")
    fun deleteAfterEnds(ts: Long)

    @Query("DELETE FROM allow_number")
    fun deleteAll()

    @Query("SELECT * FROM allow_number WHERE ttl > :ts")
    fun selectBeforeEnds(ts: Long): List<AllowNumber>

    fun selectActive() = selectBeforeEnds(Utils.currentTimeSeconds())
    fun deleteExpired() = deleteAfterEnds(Utils.currentTimeSeconds())
    fun update(obj: AllowNumber) = updateTimestamp(obj.ttl, obj.phoneNumber)
}

@Entity(
    indices = [Index(value = ["phone_number"], unique = true), Index(value = ["ttl"])],
    tableName = "allow_number",
)
data class AllowNumber(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo(name = "phone_number") val phoneNumber: String,
    @ColumnInfo(name = "ttl") val ttl: Long,
) {
    companion object {
        fun new(phoneNumber: Phonenumber.PhoneNumber, minutes: Int): AllowNumber {
            return AllowNumber(
                0,
                PhoneNumberUtil
                    .getInstance()
                    .format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164),
                Utils.currentTimeSeconds() + minutes * 60L,
            )
        }
    }
}