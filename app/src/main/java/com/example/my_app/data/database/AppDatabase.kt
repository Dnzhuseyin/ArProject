package com.example.my_app.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.example.my_app.data.model.*

@Database(
    entities = [
        User::class,
        Exercise::class,
        ExerciseSession::class,
        Message::class,
        Achievement::class,
        DailyTask::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun exerciseSessionDao(): ExerciseSessionDao
    abstract fun messageDao(): MessageDao
    abstract fun achievementDao(): AchievementDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "physiotherapy_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}