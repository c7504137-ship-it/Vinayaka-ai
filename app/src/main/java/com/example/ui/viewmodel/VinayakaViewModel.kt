package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.AgentLog
import com.example.data.model.CompanionChat
import com.example.data.model.MangaStoryboard
import com.example.data.model.StudyPlan
import com.example.data.repository.RoutingResult
import com.example.data.repository.VinayakaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

class VinayakaViewModel(
    application: Application,
    private val repository: VinayakaRepository
) : AndroidViewModel(application) {

    // Reactive states from Database
    val chatHistory: StateFlow<List<CompanionChat>> = repository.chatHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val storyboards: StateFlow<List<MangaStoryboard>> = repository.storyboards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studyPlans: StateFlow<List<StudyPlan>> = repository.studyPlans
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val agentLogs: StateFlow<List<AgentLog>> = repository.agentLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI state parameters
    var currentToolSelected by mutableStateOf("AI Router: Orchestrator Idle")
        private set

    var currentModelUsed by mutableStateOf("Models Standby")
        private set

    var currentAvatarState by mutableStateOf("Neutral") // "Thinking", "Happy", "Encouraging", "Neutral"
        private set

    var isThinking by mutableStateOf(false)
        private set

    // Soundwave audio simulation states
    var isRecordingVoicemail by mutableStateOf(false)
        private set
    var isSimulatingSpeechType by mutableStateOf("None") // "Listening", "Speaking", "None"
        private set
    var micPower = MutableStateFlow(0f)
    val micPowerState = micPower.asStateFlow()

    // Mock system system controls
    var mockBrightness by mutableStateOf(0.75f)
        private set
    var mockRingerMode by mutableStateOf("Normal") // "Normal", "Vibrate", "Silent"
        private set
    var mockStorageOptimizedPercent by mutableStateOf(84)
        private set

    // Ambient Lock Screen display simulator
    var showAmbientLockScreen by mutableStateOf(false)

    // Current inputs
    var chatInputField by mutableStateOf("")

    init {
        // Let's seed initial logs to give a warm informative feel
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertAgentLog("System Boot", "System Control", "Completed", "Vinayaka AI Core loaded successfully on Android container. All threads initialized.")
            repository.insertAgentLog("Memory Sync", "System Control", "Completed", "Room Database verified. Local caches are ready to secure user creations.")
        }
    }

    // AI Orchestrator Main Interface
    fun sendMessage(prompt: String) {
        if (prompt.isBlank()) return
        chatInputField = ""
        isThinking = true
        currentAvatarState = "Thinking"
        currentToolSelected = "Analyzing request structure..."
        currentModelUsed = "Resolving AI weight models..."

        viewModelScope.launch {
            // Give a realistic brief thinking lag to visual show orchestrator routing
            delay(800)

            val result = repository.routeAndExecute(prompt)
            isThinking = false

            when (result) {
                is RoutingResult.Success -> {
                    currentToolSelected = result.toolSelected
                    currentModelUsed = result.modelUsed
                    currentAvatarState = result.emotionState
                }
                is RoutingResult.Error -> {
                    currentToolSelected = "Error Recovery Router"
                    currentModelUsed = "Local Fallback Module"
                    currentAvatarState = "Encouraging"
                }
            }
        }
    }

    // Simulated Voice wake command activation
    fun triggerVoiceWake() {
        if (isThinking) return
        isSimulatingSpeechType = "Listening"
        currentAvatarState = "Thinking"
        currentToolSelected = "Voice Detection Sensor"
        currentModelUsed = "Wake-word: 'Hey Vinayaka'"
        
        viewModelScope.launch {
            repository.insertAgentLog("Wake-word triggered", "System Control", "Completed", "Simulating hands-free 'Hey Vinayaka' microphone activation.")
            // Simulate reading sound levels from microphone standard deviation
            for (i in 1..15) {
                micPower.value = Random.nextFloat() * 0.8f + 0.2f
                delay(120)
            }
            micPower.value = 0f
            isSimulatingSpeechType = "Speaking"
            currentAvatarState = "Happy"
            
            val greetings = listOf(
                "Greetings, friend! My voice receptors are active. How can I help you compile your notes, refine your manga story, or optimize system parameters?",
                "Command observed. Vinayaka voice-pipeline is responsive. What should we create today?",
                "Indeed, I am here! Say the word and your creative ideas will be structured."
            )
            val selectedGreeting = greetings.random()
            
            // Send speaking answer
            repository.insertAgentLog("Voice Synthesis", "System Control", "Completed", "Returned processed vocal answer feedback.")
            repository.routeAndExecute("System Wake request vocal response") // updates DB logs
            
            // Clear simulated audio view
            delay(1000)
            isSimulatingSpeechType = "None"
            currentAvatarState = "Neutral"
        }
    }

    // simulated Speech synthesis loop
    fun runSpeechSynthesis(text: String) {
        if (isSimulatingSpeechType != "None") return
        isSimulatingSpeechType = "Speaking"
        currentAvatarState = "Encouraging"
        viewModelScope.launch {
            repository.insertAgentLog("TTS Active", "System Control", "Completed", "Playing vocal audio readout stream: '${text.take(35)}...'")
            // Generate visual soundwave motion
            for (i in 1..20) {
                micPower.value = Random.nextFloat() * 0.7f + 0.3f
                delay(100)
            }
            micPower.value = 0f
            isSimulatingSpeechType = "None"
            currentAvatarState = "Neutral"
        }
    }

    // Creative Manga Generation Action
    fun executeMangaPipeline(title: String, prompt: String, idea: String) {
        viewModelScope.launch {
            val processedTitle = title.ifBlank { "Untitled Manga Concept" }
            val processedPrompt = prompt.ifBlank { "Minimal anime concept with mechanical suit" }
            val processedIdea = idea.ifBlank { "A short panels script outlining character entering an antique gear shop." }
            
            val mockFramesJson = """
                [
                  {"frame": 1, "panel": "Establishment shot of the historic gear shop. Dust motes in the air."},
                  {"frame": 2, "panel": "Hero peer into the counter. Protagonist's mechanical eye glows slightly."},
                  {"frame": 3, "panel": "Shopkeeper hands a spherical item. Text: 'You've returned, Vinayaka.'"}
                ]
            """.trimIndent()

            repository.addStoryboard(
                title = processedTitle,
                prompt = processedPrompt,
                idea = processedIdea,
                frameDetails = mockFramesJson
            )
        }
    }

    // Study Plan Builder Action
    fun executeCalculusSyllabusStudy(subject: String, topic: String, concept: String) {
        viewModelScope.launch {
            val processedSubject = subject.ifBlank { "Calculus BC" }
            val processedTopic = topic.ifBlank { "Fundamental Theorem of Calculus" }
            val explanation = if (concept.isNotBlank()) concept else {
                "The Fundamental Theorem of Calculus establishes a connection between differentiation and integration. " +
                "Specifically, it states that if f is continuous, then the integral of f from a to b can be computed using any antiderivative F: " +
                "∫ (from a to b) f(x)dx = F(b) - F(a). This fundamentally reduces accumulation calculations into straightforward net-change problems."
            }

            val mockQuizJson = """
                [
                  {"q": "What links differentiation and integration continuous systems?", "a": "The Fundamental Theorem of Calculus"},
                  {"q": "If F(x) is the integral of f(t) dt from a to x, what is F'(x)?", "a": "f(x)"}
                ]
            """.trimIndent()

            repository.addStudyPlan(processedSubject, processedTopic, explanation, mockQuizJson)
        }
    }

    // Remove item commands
    fun deleteStoryboard(storyboard: MangaStoryboard) {
        viewModelScope.launch {
            repository.deleteStoryboard(storyboard)
        }
    }

    fun deleteStudyPlan(studyPlan: StudyPlan) {
        viewModelScope.launch {
            repository.deleteStudyPlan(studyPlan)
        }
    }

    fun clearAllChats() {
        viewModelScope.launch {
            repository.clearChats()
        }
    }

    fun clearAllLogs() {
        viewModelScope.launch {
            repository.clearLogs()
        }
    }

    // Mock device controls
    fun updateMockBrightness(level: Float) {
        mockBrightness = level
        viewModelScope.launch {
            repository.insertAgentLog(
                "Modify Display settings",
                "System Control",
                "Completed",
                "Simulated system action: brightness level set to ${(level * 100).toInt()}%"
            )
        }
    }

    fun cycleRinger() {
        mockRingerMode = when (mockRingerMode) {
            "Normal" -> "Vibrate"
            "Vibrate" -> "Silent"
            else -> "Normal"
        }
        viewModelScope.launch {
            repository.insertAgentLog(
                "Ringer adjust setting",
                "System Control",
                "Completed",
                "Simulated phone ringer changed to $mockRingerMode mode"
            )
        }
    }

    fun optimizeMockStorage() {
        viewModelScope.launch {
            repository.insertAgentLog("Storage Optimize check", "System Control", "In Progress", "Running garbage collection and compressing local application cache...")
            delay(1200)
            mockStorageOptimizedPercent = Random.nextInt(92, 99)
            repository.insertAgentLog("Storage Optimization complete", "System Control", "Completed", "Successfully recovered 1.4GB of mock temp memory files. Optimised total score: $mockStorageOptimizedPercent%")
        }
    }
}

class VinayakaViewModelFactory(
    private val application: Application,
    private val repository: VinayakaRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VinayakaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VinayakaViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
