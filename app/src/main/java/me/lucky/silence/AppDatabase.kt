package me.lucky.silence

import android.content.Context
import androidx.room.*
import androidx.room.migration.AutoMigrationSpec

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber

@Database(
    entities = [TmpNumber::class],
    version = 2,
    autoMigrations = [AutoMigration(from = 1, to = 2, spec = AutoMigration1to2::class)],
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tmpNumberDao(): TmpNumberDao

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
                .databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                .allowMainThreadQueries()
                .build()
        }
    }
}

@RenameTable(fromTableName = "sms_filter", toTableName = "tmp_number")
private class AutoMigration1to2 : AutoMigrationSpec

@Dao
interface TmpNumberDao {
    companion object {
        const val INACTIVE_DURATION = 48 * 60 * 60
    }

    @Insert
    fun insert(obj: TmpNumber)

    @Query("UPDATE tmp_number SET ts_created = :ts WHERE phone_number = :phoneNumber")
    fun updateTimestamp(ts: Long, phoneNumber: String)

    @Query("DELETE FROM tmp_number WHERE ts_created < :ts")
    fun deleteBefore(ts: Long)

    @Query("DELETE FROM tmp_number")
    fun deleteAll()

    @Query("SELECT * FROM tmp_number WHERE ts_created > :ts")
    fun selectAfter(ts: Long): List<TmpNumber>

    fun selectActive(): List<TmpNumber> {
        return selectAfter(getInactiveTimestamp())
    }

    fun deleteInactive() {
        deleteBefore(getInactiveTimestamp())
    }

    private fun getInactiveTimestamp(): Long {
        return System.currentTimeMillis() / 1000 - INACTIVE_DURATION
    }

    fun update(obj: TmpNumber) {
        updateTimestamp(obj.tsCreated, obj.phoneNumber)
    }
}

@Entity(
    indices = [Index(value = ["phone_number"], unique = true), Index(value = ["ts_created"])],
    tableName = "tmp_number",
)
data class TmpNumber(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo(name = "phone_number") val phoneNumber: String,
    @ColumnInfo(name = "ts_created") val tsCreated: Long,
) {
    constructor(phoneNumber: Phonenumber.PhoneNumber) : this(
        0,
        PhoneNumberUtil.getInstance().format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164),
        System.currentTimeMillis() / 1000,
    )
}
