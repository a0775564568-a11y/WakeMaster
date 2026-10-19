package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AlarmEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [AlarmEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "wakemaster_database"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialAlarms(database.alarmDao())
                    }
                }
            }

            private suspend fun populateInitialAlarms(dao: AlarmDao) {
                // Pre-populate with two high-quality example alarms
                dao.insert(
                    AlarmEntity(
                        hour = 7,
                        minute = 0,
                        label = "השכמה לימי עבודה",
                        isEnabled = false,
                        daysOfWeek = listOf(1, 2, 3, 4, 5), // Sun - Thu
                        missionType = "MATH",
                        missionDifficulty = "MEDIUM",
                        missionCount = 2,
                        gradualVolume = true,
                        volume = 0.9f,
                        lockTaskMode = true
                    )
                )
                dao.insert(
                    AlarmEntity(
                        hour = 8,
                        minute = 30,
                        label = "סוף שבוע רגוע",
                        isEnabled = false,
                        daysOfWeek = listOf(6, 7), // Fri - Sat
                        missionType = "MEMORY",
                        missionDifficulty = "EASY",
                        missionCount = 1,
                        gradualVolume = true,
                        volume = 0.8f,
                        lockTaskMode = false
                    )
                )
            }
        }
    }
}
