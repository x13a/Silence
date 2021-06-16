package me.lucky.silence

import android.content.Context

import androidx.room.*

@Database(entities = [SmsFilter::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun smsFilterDao(): SmsFilterDao

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

@Dao
interface SmsFilterDao {
    companion object {
        const val INACTIVE_DURATION = 48 * 60 * 60
    }

    @Insert
    fun insert(obj: SmsFilter)

    @Update
    fun update(obj: SmsFilter)

    @Query("DELETE FROM sms_filter WHERE ts_created < :ts")
    fun deleteBefore(ts: Long)

    @Query("SELECT * FROM sms_filter WHERE ts_created > :ts")
    fun getAfter(ts: Long): List<SmsFilter>

    fun getActive(): List<SmsFilter> {
        return getAfter(getInactiveTimestamp())
    }

    fun deleteInactive() {
        deleteBefore(getInactiveTimestamp())
    }

    private fun getInactiveTimestamp(): Long {
        return System.currentTimeMillis() / 1000 - INACTIVE_DURATION
    }
}

@Entity(
    indices = [Index(value = ["phone_number"], unique = true), Index(value = ["ts_created"])],
    tableName = "sms_filter",
)
data class SmsFilter(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo(name = "phone_number") val phoneNumber: String,
    @ColumnInfo(name = "ts_created") val tsCreated: Long,
) {
    companion object {
        fun new(phoneNumber: String): SmsFilter {
            return SmsFilter(0, phoneNumber, System.currentTimeMillis() / 1000)
        }
    }
}
