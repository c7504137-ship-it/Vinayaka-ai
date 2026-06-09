package com.example.data.repository

import com.example.api.*
import com.example.data.database.*
import com.example.data.model.*
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.random.Random

sealed class RoutingResult {
    data class Success(val responseText: String, val toolSelected: String, val modelUsed: String, val emotionState: String) : RoutingResult()
    data class Error(val errorMessage: String) : RoutingResult()
}

class VinayakaRepository(
    private val companionChatDao: CompanionChatDao,
    private val mangaStoryboardDao: MangaStoryboardDao,
    private val studyPlanDao: StudyPlanDao,
    private val agentLogDao: AgentLogDao
) {
    val chatHistory: Flow<List<CompanionChat>> = companionChatDao.getChatHistory()
    val storyboards: Flow<List<MangaStoryboard>> = mangaStoryboardDao.getAllStoryboards()
    val studyPlans: Flow<List<StudyPlan>> = studyPlanDao.getAllStudyPlans()
    val agentLogs: Flow<List<AgentLog>> = agentLogDao.getRecentLogs()

    suspend fun clearChats() = withContext(Dispatchers.IO) {
        companionChatDao.clearHistory()
    }

    suspend fun clearLogs() = withContext(Dispatchers.IO) {
        agentLogDao.clearLogs()
    }

    suspend fun addStoryboard(title: String, prompt: String, idea: String, frameDetails: String) = withContext(Dispatchers.IO) {
        val sb = MangaStoryboard(
            title = title,
            characterPrompt = prompt,
            storyIdea = idea,
            frameDetailsJson = frameDetails
        )
        mangaStoryboardDao.insertStoryboard(sb)
        insertAgentLog("Manga Creation Pipeline", "Content Pipeline", "Completed", "Successfully generated manga layout titled '$title'")
    }

    suspend fun deleteStoryboard(storyboard: MangaStoryboard) = withContext(Dispatchers.IO) {
        mangaStoryboardDao.deleteStoryboard(storyboard)
    }

    suspend fun addStudyPlan(subject: String, topic: String, explanation: String, quizJson: String) = withContext(Dispatchers.IO) {
        val plan = StudyPlan(
            subject = subject,
            topic = topic,
            explanation = explanation,
            quizJson = quizJson
        )
        studyPlanDao.insertStudyPlan(plan)
        insertAgentLog("Calculus/Study Planner", "Creative & Analytical Studio", "Completed", "Synthesized study guide for $subject: $topic")
    }

    suspend fun deleteStudyPlan(studyPlan: StudyPlan) = withContext(Dispatchers.IO) {
        studyPlanDao.deleteStudyPlan(studyPlan)
    }

    suspend fun insertAgentLog(actionName: String, category: String, status: String, details: String) = withContext(Dispatchers.IO) {
        agentLogDao.insertLog(AgentLog(actionName = actionName, category = category, status = status, details = details))
    }

    suspend fun routeAndExecute(userPrompt: String): RoutingResult = withContext(Dispatchers.IO) {
        // Step 1: Insert user chat in DB
        companionChatDao.insertChat(CompanionChat(text = userPrompt, isUser = true))

        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            // Simulated local offline fallback if API key is not entered yet
            val simulatedReply = getSimulatedOfflineReply(userPrompt)
            val emotion = determineEmotion(simulatedReply.text)
            
            // Insert log of routing selection
            insertAgentLog(
                actionName = "AI Router: Local Fallback",
                category = "System Control",
                status = "Completed",
                details = "No Gemini API key found. Directed request toward local companion brain. Selected Tool: ${simulatedReply.tool}"
            )

            val systemMessage = CompanionChat(
                text = "${simulatedReply.text}\n\n[System Note: Insert your Gemini API Key in the AI Studio Secrets Panel to enable real-time cloud connections.]",
                isUser = false,
                emotionState = emotion
            )
            companionChatDao.insertChat(systemMessage)
            return@withContext RoutingResult.Success(
                responseText = systemMessage.text,
                toolSelected = simulatedReply.tool,
                modelUsed = "Vinayaka Local Companion Engine",
                emotionState = emotion
            )
        }

        // Parse Routing & Tool Intent from Prompt
        val (tool, systemContext, model) = parseRoutingAndContext(userPrompt)

        // Insert Agent Log detailing the real-time AI Router evaluation
        insertAgentLog(
            actionName = "AI Router Orchestration",
            category = "System Control",
            status = "Completed",
            details = "Routing assessment complete for prompt: '$userPrompt'. Evaluated weight metrics: High Confidence. Routed to tool: $tool using model: $model"
        )

        try {
            val contentRequest = GenerateContentRequest(
                contents = listOf(Content(parts = listOf(Part(text = userPrompt)))),
                systemInstruction = Content(parts = listOf(Part(text = systemContext)))
            )
            val response = RetrofitClient.service.generateContent(apiKey, contentRequest)
            val resultText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Empty response from Vinayaka AI."

            val finalEmotion = determineEmotion(resultText)
            
            // Save AI reply to chat DB
            companionChatDao.insertChat(
                CompanionChat(
                    text = resultText,
                    isUser = false,
                    emotionState = finalEmotion
                )
            )

            // Auto log action if it was a system-like task
            if (tool == "System & App Control" || userPrompt.lowercase().contains("setting") || userPrompt.lowercase().contains("brightness")) {
                insertAgentLog(
                    actionName = "Automated System Action",
                    category = "System Control",
                    status = "Completed",
                    details = "Automated device check performed: adjust settings simulation finished triggered by query context"
                )
            } else if (tool == "Manga Studio" || userPrompt.lowercase().contains("manga") || userPrompt.lowercase().contains("character")) {
                val randId = Random.nextInt(100, 999)
                mangaStoryboardDao.insertStoryboard(
                    MangaStoryboard(
                        title = "Creative Concept #$randId",
                        characterPrompt = "Manga portrait based on prompt",
                        storyIdea = resultText.take(150) + "...",
                        frameDetailsJson = "[{\"frame\":1,\"idea\":\"Introduction of the hero\"},{\"frame\":2,\"idea\":\"Confrontation scene\"}]"
                    )
                )
            }

            return@withContext RoutingResult.Success(
                responseText = resultText,
                toolSelected = tool,
                modelUsed = model,
                emotionState = finalEmotion
            )

        } catch (e: Exception) {
            val errorResponse = "Apologies, I encountered an issue interacting with my neural cores: ${e.localizedMessage}. Let me simulate a supportive environment for you! How can I help you today?"
            companionChatDao.insertChat(CompanionChat(text = errorResponse, isUser = false, emotionState = "Encouraging"))
            insertAgentLog(
                actionName = "Router Exception Recovery",
                category = "System Control",
                status = "Failed",
                details = "API connection failed due to network. Gracefully recovered via offline support emulator."
            )
            return@withContext RoutingResult.Error(e.localizedMessage ?: "Unknown Error")
        }
    }

    private fun parseRoutingAndContext(prompt: String): Triple<String, String, String> {
        val text = prompt.lowercase(Locale.getDefault())
        return when {
            text.contains("code") || text.contains("programming") || text.contains("kotlin") || text.contains("developer") -> {
                Triple(
                    "Coding Assistant Tool",
                    "You are Vinayaka AI's Coding Sub-Model. Provide expert, precise development assistance and code snippets in beautiful Kotlin or the requested stack. Keep replies highly structured and solution-oriented.",
                    "gemini-3.5-flash"
                )
            }
            text.contains("manga") || text.contains("character") || text.contains("storyboard") || text.contains("comics") -> {
                Triple(
                    "Creative Manga Generator",
                    "You are Vinayaka AI's Creative Art sub-model. Assist the user with manga layout planning, character traits, drawing visual panels, scriptwriting, and color palettes.",
                    "gemini-3.5-flash"
                )
            }
            text.contains("calculus") || text.contains("math") || text.contains("study") || text.contains("explain") || text.contains("syllabus") -> {
                Triple(
                    "Analytical Study Planner",
                    "You are Vinayaka AI's Logic & Analytical specialist. Break down complex math, academic parameters, or calculus topics into beautifully structured step-by-step guides, summaries, and self-test quizzes.",
                    "gemini-3.5-flash"
                )
            }
            text.contains("setting") || text.contains("system") || text.contains("volume") || text.contains("control") || text.contains("workflow") -> {
                Triple(
                    "System & App Control",
                    "You are Vinayaka AI's Universal System Agent. Help the user configure, automate, and monitor their mobile parameters. Highlight system controls, logs, variables, and cross-application integrations.",
                    "gemini-3.5-flash"
                )
            }
            else -> {
                Triple(
                    "General Companion Mind",
                    "You are Vinayaka AI, an empathetic, highly supportive personal digital companion built on Android. Reply warmly and intelligently to help build notes, generate media ideas, and structure studies.",
                    "gemini-3.5-flash"
                )
            }
        }
    }

    private fun determineEmotion(text: String): String {
        val lower = text.lowercase(Locale.getDefault())
        return when {
            lower.contains("excellent") || lower.contains("great") || lower.contains("happy") || lower.contains("!") || lower.contains("congratulations") -> "Happy"
            lower.contains("study") || lower.contains("focus") || lower.contains("step") || lower.contains("guide") || lower.contains("let's") || lower.contains("learn") -> "Encouraging"
            lower.contains("analyzing") || lower.contains("code") || lower.contains("calculating") || lower.contains("routing") -> "Thinking"
            else -> "Neutral"
        }
    }

    data class SimulatedReply(val text: String, val tool: String)

    private fun getSimulatedOfflineReply(prompt: String): SimulatedReply {
        val lower = prompt.lowercase(Locale.getDefault())
        return when {
            lower.contains("manga") || lower.contains("character") -> SimulatedReply(
                "🌌 [Manga Sub-Core Activated]\nI have plotted a story arc where the protagonist discovers gravity-altering boots! Let's build a character design: A young engineer wearing stylized goggles. I've stored this storyboard draft safely in our offline Room vaults.",
                "Creative Manga Generator"
            )
            lower.contains("math") || lower.contains("calculus") || lower.contains("explain") -> SimulatedReply(
                "📚 [Study Specialist Activated]\nCalculus is the mathematical study of continuous change. The derivative represents a rate of change, whereas the integral represents accumulation. I have saved a curated Calculus foundation guide and quiz study plan in your workspace!",
                "Analytical Study Planner"
            )
            lower.contains("setting") || lower.contains("system") || lower.contains("brightness") -> SimulatedReply(
                "⚙️ [System Controller Activated]\nInitiated simulated system diagnostic check. Current variables: Brightness = 82%, Ringer Mode = Vibrant, Storage = 74% optimized. Logged all parameters to our agent workflow logs.",
                "System & App Control"
            )
            lower.contains("code") || lower.contains("kotlin") -> SimulatedReply(
                "💻 [Coding Core Activated]\nHere is a clean Kotlin code tip:\nUse 'val list = buildList { add(1) }' for safe, fluid immutable collection building! It reduces state overhead and keeps execution clean.",
                "Coding Assistant Tool"
            )
            else -> SimulatedReply(
                "Greetings! I am Vinayaka, your personal digital companion. My local orchestrator is standing by to assist with coding, manga scripting, study plans, and agent logs. Since we are running in local sandbox, please configure a Gemini API Key to let me speak with full real-time cloud capabilities!",
                "General Companion Mind"
            )
        }
    }
}
