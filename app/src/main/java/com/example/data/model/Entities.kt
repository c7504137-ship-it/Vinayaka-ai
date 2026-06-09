package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "companion_chats")
data class CompanionChat(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val emotionState: String = "Neutral" // e.g., "Thinking", "Happy", "Encouraging", "Neutral"
)

@Entity(tableName = "manga_storyboards")
data class MangaStoryboard(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val characterPrompt: String,
    val storyIdea: String,
    val frameDetailsJson: String, // Keep it as JSON string for easy preservation
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "study_plans")
data class StudyPlan(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subject: String,
    val topic: String,
    val explanation: String,
    val quizJson: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "agent_logs")
data class AgentLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val actionName: String,
    val category: String, // "System Control", "App Workflow", "Content Pipeline"
    val status: String,   // "Completed", "In Progress", "Failed"
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)
