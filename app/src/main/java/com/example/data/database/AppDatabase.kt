package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.AgentLog
import com.example.data.model.CompanionChat
import com.example.data.model.MangaStoryboard
import com.example.data.model.StudyPlan

@Database(
    entities = [
        CompanionChat::class,
        MangaStoryboard::class,
        StudyPlan::class,
        AgentLog::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun companionChatDao(): CompanionChatDao
    abstract fun mangaStoryboardDao(): MangaStoryboardDao
    abstract fun studyPlanDao(): StudyPlanDao
    abstract fun agentLogDao(): AgentLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vinayaka_companion_db"
                ).fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
