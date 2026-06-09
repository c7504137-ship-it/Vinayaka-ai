package com.example.data.database

import androidx.room.*
import com.example.data.model.AgentLog
import com.example.data.model.CompanionChat
import com.example.data.model.MangaStoryboard
import com.example.data.model.StudyPlan
import kotlinx.coroutines.flow.Flow

@Dao
interface CompanionChatDao {
    @Query("SELECT * FROM companion_chats ORDER BY timestamp ASC")
    fun getChatHistory(): Flow<List<CompanionChat>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: CompanionChat)

    @Query("DELETE FROM companion_chats")
    suspend fun clearHistory()
}

@Dao
interface MangaStoryboardDao {
    @Query("SELECT * FROM manga_storyboards ORDER BY timestamp DESC")
    fun getAllStoryboards(): Flow<List<MangaStoryboard>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStoryboard(storyboard: MangaStoryboard)

    @Delete
    suspend fun deleteStoryboard(storyboard: MangaStoryboard)
}

@Dao
interface StudyPlanDao {
    @Query("SELECT * FROM study_plans ORDER BY timestamp DESC")
    fun getAllStudyPlans(): Flow<List<StudyPlan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyPlan(studyPlan: StudyPlan)

    @Delete
    suspend fun deleteStudyPlan(studyPlan: StudyPlan)
}

@Dao
interface AgentLogDao {
    @Query("SELECT * FROM agent_logs ORDER BY timestamp DESC LIMIT 50")
    fun getRecentLogs(): Flow<List<AgentLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AgentLog)

    @Query("DELETE FROM agent_logs")
    suspend fun clearLogs()
}
