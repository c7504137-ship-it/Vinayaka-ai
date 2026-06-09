package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AgentLog
import com.example.data.model.CompanionChat
import com.example.data.model.MangaStoryboard
import com.example.data.model.StudyPlan
import com.example.ui.theme.*
import com.example.ui.viewmodel.VinayakaViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sin

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainCompanionScreen(viewModel: VinayakaViewModel) {
    val chatHistory by viewModel.chatHistory.collectAsStateWithLifecycle()
    val storyboards by viewModel.storyboards.collectAsStateWithLifecycle()
    val studyPlans by viewModel.studyPlans.collectAsStateWithLifecycle()
    val agentLogs by viewModel.agentLogs.collectAsStateWithLifecycle()
    val micPower by viewModel.micPowerState.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf("orchestrator") } // "orchestrator", "avatar", "control", "studio", "glasses"
    
    // Manga designer states
    var mangaTitleInput by remember { mutableStateOf("") }
    var mangaPromptInput by remember { mutableStateOf("") }
    var mangaIdeaInput by remember { mutableStateOf("") }
    var drawingPoints = remember { mutableStateListOf<Offset>() }

    // Calculus study states
    var calculusTopicInput by remember { mutableStateOf("") }
    var calculusConceptInput by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val chatListState = rememberLazyListState()

    // Auto scroll chat list to end when new messages arrive
    LaunchedEffect(chatHistory.size) {
        if (chatHistory.isNotEmpty()) {
            chatListState.animateScrollToItem(chatHistory.size - 1)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(OceanBlack)
    ) {
        val isWideScreen = maxWidth > 600.dp

        // Check if Lock Screen Simulator is ON
        if (viewModel.showAmbientLockScreen) {
            LockScreenSimulator(viewModel = viewModel)
        } else {
            Row(modifier = Modifier.fillMaxSize()) {
                // NAVIGATION RAIL (Shown on Tablets/Wide screen to stay adaptive-always)
                if (isWideScreen) {
                    NavigationRail(
                        containerColor = DeepCharcoal,
                        contentColor = PureWhite,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "Logo",
                            tint = CyberTeal,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.weight(1f))

                        NavigationRailItem(
                            selected = selectedTab == "orchestrator",
                            onClick = { selectedTab = "orchestrator" },
                            icon = { Icon(Icons.Default.Hub, contentDescription = "Orchestrator") },
                            label = { Text("Router", fontSize = 11.sp) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = OceanBlack,
                                selectedTextColor = CyberTeal,
                                indicatorColor = CyberTeal,
                                unselectedIconColor = MutedSilver,
                                unselectedTextColor = MutedSilver
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        NavigationRailItem(
                            selected = selectedTab == "avatar",
                            onClick = { selectedTab = "avatar" },
                            icon = { Icon(Icons.Default.Face, contentDescription = "Avatar") },
                            label = { Text("Companion", fontSize = 11.sp) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = OceanBlack,
                                selectedTextColor = CyberTeal,
                                indicatorColor = CyberTeal,
                                unselectedIconColor = MutedSilver,
                                unselectedTextColor = MutedSilver
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        NavigationRailItem(
                            selected = selectedTab == "control",
                            onClick = { selectedTab = "control" },
                            icon = { Icon(Icons.Default.Settings, contentDescription = "Control") },
                            label = { Text("Command", fontSize = 11.sp) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = OceanBlack,
                                selectedTextColor = CyberTeal,
                                indicatorColor = CyberTeal,
                                unselectedIconColor = MutedSilver,
                                unselectedTextColor = MutedSilver
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        NavigationRailItem(
                            selected = selectedTab == "studio",
                            onClick = { selectedTab = "studio" },
                            icon = { Icon(Icons.Default.AutoStories, contentDescription = "Studio") },
                            label = { Text("Studio", fontSize = 11.sp) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = OceanBlack,
                                selectedTextColor = CyberTeal,
                                indicatorColor = CyberTeal,
                                unselectedIconColor = MutedSilver,
                                unselectedTextColor = MutedSilver
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        NavigationRailItem(
                            selected = selectedTab == "glasses",
                            onClick = { selectedTab = "glasses" },
                            icon = { Icon(Icons.Default.RemoveRedEye, contentDescription = "Vision") },
                            label = { Text("Ambient", fontSize = 11.sp) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = OceanBlack,
                                selectedTextColor = CyberTeal,
                                indicatorColor = CyberTeal,
                                unselectedIconColor = MutedSilver,
                                unselectedTextColor = MutedSilver
                            )
                        )
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }

                // MAIN CONTENT CONTAINER
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .statusBarsPadding()
                ) {
                    // Header Bar
                    HeaderBar(
                        avatarState = viewModel.currentAvatarState,
                        modelUsed = viewModel.currentModelUsed,
                        toolSelected = viewModel.currentToolSelected,
                        onAmbientToggle = { viewModel.showAmbientLockScreen = true }
                    )

                    // Tab Body Panel with smooth slide animations
                    Box(modifier = Modifier.weight(1f)) {
                        when (selectedTab) {
                            "orchestrator" -> OrchestratorPane(
                                viewModel = viewModel,
                                chatHistory = chatHistory,
                                listState = chatListState
                            )
                            "avatar" -> CompanionAvatarPane(
                                viewModel = viewModel,
                                micPower = micPower
                            )
                            "control" -> CommandControlPane(
                                viewModel = viewModel,
                                agentLogs = agentLogs
                            )
                            "studio" -> CreativeStudioPane(
                                viewModel = viewModel,
                                storyboards = storyboards,
                                studyPlans = studyPlans,
                                titleInput = mangaTitleInput,
                                onTitleChange = { mangaTitleInput = it },
                                promptInput = mangaPromptInput,
                                onPromptChange = { mangaPromptInput = it },
                                ideaInput = mangaIdeaInput,
                                onIdeaChange = { mangaIdeaInput = it },
                                drawingPoints = drawingPoints,
                                topicInput = calculusTopicInput,
                                onTopicChange = { calculusTopicInput = it },
                                conceptInput = calculusConceptInput,
                                onConceptChange = { calculusConceptInput = it }
                            )
                            "glasses" -> SmartGlassesVisionPane()
                        }
                    }

                    // BOTTOM NAVIGATION BAR (Shown on Compact screens for perfect mobile layouts)
                    if (!isWideScreen) {
                        NavigationBar(
                            containerColor = DeepCharcoal,
                            contentColor = PureWhite,
                            modifier = Modifier.navigationBarsPadding()
                        ) {
                            NavigationBarItem(
                                selected = selectedTab == "orchestrator",
                                onClick = { selectedTab = "orchestrator" },
                                icon = { Icon(Icons.Default.Hub, contentDescription = "Router") },
                                label = { Text("Router", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = OceanBlack,
                                    selectedTextColor = CyberTeal,
                                    indicatorColor = CyberTeal,
                                    unselectedIconColor = MutedSilver,
                                    unselectedTextColor = MutedSilver
                                )
                            )
                            NavigationBarItem(
                                selected = selectedTab == "avatar",
                                onClick = { selectedTab = "avatar" },
                                icon = { Icon(Icons.Default.Face, contentDescription = "Avatar") },
                                label = { Text("Companion", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = OceanBlack,
                                    selectedTextColor = CyberTeal,
                                    indicatorColor = CyberTeal,
                                    unselectedIconColor = MutedSilver,
                                    unselectedTextColor = MutedSilver
                                )
                            )
                            NavigationBarItem(
                                selected = selectedTab == "control",
                                onClick = { selectedTab = "control" },
                                icon = { Icon(Icons.Default.Settings, contentDescription = "Command") },
                                label = { Text("Command", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = OceanBlack,
                                    selectedTextColor = CyberTeal,
                                    indicatorColor = CyberTeal,
                                    unselectedIconColor = MutedSilver,
                                    unselectedTextColor = MutedSilver
                                )
                            )
                            NavigationBarItem(
                                selected = selectedTab == "studio",
                                onClick = { selectedTab = "studio" },
                                icon = { Icon(Icons.Default.AutoStories, contentDescription = "Studio") },
                                label = { Text("Studio", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = OceanBlack,
                                    selectedTextColor = CyberTeal,
                                    indicatorColor = CyberTeal,
                                    unselectedIconColor = MutedSilver,
                                    unselectedTextColor = MutedSilver
                                )
                            )
                            NavigationBarItem(
                                selected = selectedTab == "glasses",
                                onClick = { selectedTab = "glasses" },
                                icon = { Icon(Icons.Default.RemoveRedEye, contentDescription = "Ambient") },
                                label = { Text("Ambient", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = OceanBlack,
                                    selectedTextColor = CyberTeal,
                                    indicatorColor = CyberTeal,
                                    unselectedIconColor = MutedSilver,
                                    unselectedTextColor = MutedSilver
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

// ------------------- SUB PANELS & WORKSPACES -------------------

@Composable
fun HeaderBar(
    avatarState: String,
    modelUsed: String,
    toolSelected: String,
    onAmbientToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left Area: Glowing Orb + Text details
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Simulated Android Blur Gradient Orb
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SleekCharcoal)
                    .border(1.dp, SleekBorderLight, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Sleek blur effect simulation via nested semi-transparent glowing shapes
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(SleekLightBlue, SleekViolet)
                            )
                        )
                )
            }

            Column {
                Text(
                    text = "Vinayaka AI",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = PureWhite,
                    letterSpacing = (-0.2).sp
                )
                
                // Pulsing Green Indicator + System status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val infinitePulse = rememberInfiniteTransition(label = "pulse_green")
                    val greenAlpha by infinitePulse.animateFloat(
                        initialValue = 0.4f,
                        targetValue = 1.0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "green_pulse"
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .graphicsLayer(alpha = greenAlpha)
                            .background(SleekGreen, CircleShape)
                    )
                    Text(
                        text = "SYSTEM ONLINE  •  $toolSelected",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextMuted,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        // Right Area: The tactile multifunction menu button / ambient lock trigger
        Card(
            modifier = Modifier
                .size(40.dp)
                .clickable { onAmbientToggle() },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SleekCharcoal),
            border = BorderStroke(1.dp, SleekBorderDark)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⠿",
                    color = PureWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// Tab 1: AI Orchestrator Pane
@Composable
fun OrchestratorPane(
    viewModel: VinayakaViewModel,
    chatHistory: List<CompanionChat>,
    listState: androidx.compose.foundation.lazy.LazyListState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
    ) {
        // Chat List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            state = listState,
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            if (chatHistory.isEmpty()) {
                item {
                    EmptyStatePlaceholder(
                        title = "Empathetic Core Initialized",
                        desc = "Greetings! I am Vinayaka, an advanced orchestrator. Ask me to draft story arcs, outline calculus proofs, or run checks on your simulated hardware arrays."
                    )
                }
            } else {
                items(chatHistory) { chat ->
                    ChatBubble(chat = chat, onSpeak = { viewModel.runSpeechSynthesis(chat.text) })
                }
                if (viewModel.isThinking) {
                    item {
                        ThinkingIndicator()
                    }
                }
            }
        }

        // Templates quick list
        Text(
            text = "Routing Templates:",
            fontSize = 11.sp,
            color = MutedSilver,
            modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val prompts = listOf(
                "Create Manga Character" to "Manga Design",
                "Explain Calculus integration" to "Calc Study",
                "Execute system settings diagnostics" to "Sys Control"
            )
            prompts.forEach { (full, label) ->
                Button(
                    onClick = { viewModel.sendMessage(full) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CardGray,
                        contentColor = CyberTeal
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Input Tray
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = viewModel.chatInputField,
                onValueChange = { viewModel.chatInputField = it },
                placeholder = { Text("Talk with Vinayaka AI...", color = MutedSilver, fontSize = 14.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberTeal,
                    unfocusedBorderColor = CardGray,
                    focusedTextColor = PureWhite,
                    unfocusedTextColor = PureWhite,
                    focusedContainerColor = DeepCharcoal,
                    unfocusedContainerColor = DeepCharcoal
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                trailingIcon = {
                    if (viewModel.chatInputField.isNotBlank()) {
                        IconButton(onClick = { viewModel.chatInputField = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = MutedSilver)
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            FloatingActionButton(
                onClick = {
                    if (viewModel.chatInputField.isNotBlank()) {
                        viewModel.sendMessage(viewModel.chatInputField)
                    }
                },
                containerColor = CyberTeal,
                contentColor = OceanBlack,
                shape = CircleShape,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("send_button")
            ) {
                Icon(Icons.Default.Send, contentDescription = "Submit message")
            }
        }
    }
}

// Tab 2: Companion Emotional Avatar Pane
@Composable
fun CompanionAvatarPane(
    viewModel: VinayakaViewModel,
    micPower: Float
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "SYSTEM TELEMETRY & EMOTION CORE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = SleekLightBlue,
            letterSpacing = 2.sp
        )

        // The Orchestrator Core (Avatar) - High Fidelity Sleek Adaptation
        Box(
            modifier = Modifier
                .size(240.dp)
                .drawBehind {
                    // Glow Backdrop Area with Radial Gradient
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(SleekBlue.copy(alpha = 0.08f), Color.Transparent),
                            center = center,
                            radius = size.width / 1.5f
                        )
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // Outer dashed border ring
            Canvas(modifier = Modifier.size(220.dp)) {
                drawCircle(
                    color = SleekBorderLight.copy(alpha = 0.35f),
                    radius = size.minDimension / 2f,
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                    )
                )
            }

            // Middle circular border
            Box(
                modifier = Modifier
                    .size(175.dp)
                    .border(1.dp, SleekBorderLight.copy(alpha = 0.45f), CircleShape)
            )

            // Inner Pulsing Core with Sleek Gradients
            val infiniteTransition = rememberInfiniteTransition(label = "pulse_core")
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 0.96f,
                targetValue = 1.04f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = LinearOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulse"
            )

            val currentColors = when (viewModel.currentAvatarState) {
                "Thinking" -> listOf(SleekBlueDark, SleekPurple, SleekPink)
                "Happy" -> listOf(SleekLightBlue, SleekViolet, SleekPink)
                "Encouraging" -> listOf(SleekPurple, SleekPink, SleekLightBlue)
                else -> listOf(SleekCharcoal, SleekBorderLight, SleekCharcoal)
            }

            Box(
                modifier = Modifier
                    .size(130.dp)
                    .graphicsLayer(
                        scaleX = pulseScale + (micPower * 0.15f),
                        scaleY = pulseScale + (micPower * 0.15f)
                    )
                    .background(
                        Brush.linearGradient(colors = currentColors),
                        shape = CircleShape
                    )
                    .shadow(32.dp, shape = CircleShape, clip = false, ambientColor = currentColors.first(), spotColor = currentColors.last()),
                contentAlignment = Alignment.Center
            ) {
                // Inner Glassy Mask
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .background(SleekBlack.copy(alpha = 0.45f), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // Waveform visualization - 4 active bars
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val waveHeights = when (viewModel.currentAvatarState) {
                            "Thinking" -> listOf(0.4f, 0.7f, 0.3f, 0.6f)
                            "Happy" -> listOf(0.5f, 0.9f, 0.6f, 0.8f)
                            "Encouraging" -> listOf(0.3f, 0.6f, 0.8f, 0.5f)
                            else -> listOf(0.2f, 0.3f, 0.2f, 0.3f)
                        }

                        waveHeights.forEachIndexed { idx, baseHeight ->
                            val animTransition = rememberInfiniteTransition(label = "wave_$idx")
                            val fluctuation by animTransition.animateFloat(
                                initialValue = 0.7f,
                                targetValue = 1.3f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(500 + idx * 80, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "fluct"
                            )
                            val scaleFactor = if (micPower > 0.01f) (1f + micPower * 3f) else fluctuation
                            val heightDp = (35 * baseHeight * scaleFactor).coerceIn(4f, 56f).dp

                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(heightDp)
                                    .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(1.5.dp))
                            )
                        }
                    }
                }
            }
        }

        // Animated Status / Thinking Prompt
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            val italicQuote = when (viewModel.currentAvatarState) {
                "Thinking" -> "\"Analyzing your morning workflow...\""
                "Happy" -> "\"Generative pipeline active. Ready to deploy creative boards.\""
                "Encouraging" -> "\"Support matrices live. Phenomenal progress logged in database.\""
                else -> "\"Standing by. Tap the controller or mic to broadcast audio query.\""
            }
            Text(
                text = italicQuote,
                fontSize = 13.sp,
                fontStyle = FontStyle.Italic,
                color = SleekTextMuted,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = when (viewModel.currentAvatarState) {
                    "Thinking" -> "Vinayaka is thinking"
                    "Happy" -> "Vinayaka is online"
                    "Encouraging" -> "Vinayaka is supportive"
                    else -> "Vinayaka is listening"
                },
                fontSize = 22.sp,
                fontWeight = FontWeight.Light,
                color = PureWhite,
                textAlign = TextAlign.Center
            )
        }

        // Agentic Suggestions Cards Grid
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "AGENTIC SUGGESTIONS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = SleekTextMuted,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Card 1: Storyboarding
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.sendMessage("Initiate new Storyboard outline") },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SleekCharcoal),
                    border = BorderStroke(1.dp, SleekBorderDark)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Text("✎", color = SleekLightBlue, fontSize = 20.sp, modifier = Modifier.padding(bottom = 4.dp))
                        Text("Storyboarding", color = PureWhite, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                        Text("Manga Studio", color = SleekTextMuted, fontSize = 10.sp)
                    }
                }

                // Card 2: App Relay
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.sendMessage("Show system diagnostics log") },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SleekCharcoal),
                    border = BorderStroke(1.dp, SleekBorderDark)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Text("⌘", color = SleekViolet, fontSize = 20.sp, modifier = Modifier.padding(bottom = 4.dp))
                        Text("App Relay", color = PureWhite, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                        Text("3 pending tasks", color = SleekTextMuted, fontSize = 10.sp)
                    }
                }
            }
        }

        // Sleek Interaction Controller
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            shape = RoundedCornerShape(40.dp),
            colors = CardDefaults.cardColors(containerColor = SleekCharcoal),
            border = BorderStroke(1.dp, SleekBorderDark),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        viewModel.sendMessage("Take diagnostics snapshot with smart glasses")
                    },
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Camera",
                        tint = SleekTextMuted
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = if (viewModel.isSimulatingSpeechType != "None") "Active transmission listening..." else "Hey Vinayaka...",
                        color = SleekTextMuted,
                        fontSize = 13.sp
                    )
                }

                IconButton(
                    onClick = { viewModel.triggerVoiceWake() },
                    modifier = Modifier
                        .size(42.dp)
                        .background(SleekBlue, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Microphone wake trigger",
                        tint = PureWhite
                    )
                }
            }
        }
    }
}

// Tab 3: Control Center / Diagnostics Pane
@Composable
fun CommandControlPane(
    viewModel: VinayakaViewModel,
    agentLogs: List<AgentLog>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Text(
            text = "UNIVERSAL AGENT CONTROL",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = CyberTeal,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // System Simulator Variable Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Brightness Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(130.dp),
                colors = CardDefaults.cardColors(containerColor = DeepCharcoal),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LightMode, contentDescription = "Brightness", tint = CyberTeal)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Brightness", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("${(viewModel.mockBrightness * 100).toInt()}%", fontSize = 20.sp, fontWeight = FontWeight.Black, color = PureWhite)
                    Slider(
                        value = viewModel.mockBrightness,
                        onValueChange = { viewModel.updateMockBrightness(it) },
                        colors = SliderDefaults.colors(
                            thumbColor = CyberTeal,
                            activeTrackColor = CyberTeal,
                            inactiveTrackColor = CardGray
                        )
                    )
                }
            }

            // Ringer Mode Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(130.dp),
                colors = CardDefaults.cardColors(containerColor = DeepCharcoal),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VolumeUp, contentDescription = "Ringer", tint = SoftGold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ringer System", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(viewModel.mockRingerMode, fontSize = 20.sp, fontWeight = FontWeight.Black, color = PureWhite)
                    Button(
                        onClick = { viewModel.cycleRinger() },
                        colors = ButtonDefaults.buttonColors(containerColor = CardGray, contentColor = CyberTeal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Cycle Mode", fontSize = 11.sp)
                    }
                }
            }
        }

        // Storage Optimization Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = DeepCharcoal),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Memory,
                    contentDescription = "Memory Optimization",
                    tint = CyberTeal,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Auto-Memory Optimization", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                    Text("Current Simulated Efficiency: ${viewModel.mockStorageOptimizedPercent}%", fontSize = 11.sp, color = MutedSilver)
                }
                Button(
                    onClick = { viewModel.optimizeMockStorage() },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberTeal, contentColor = OceanBlack),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("optimize_mem_button")
                ) {
                    Text("Defrag", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Real-time Agent Log viewer (Synced to SQLite Room Database!)
        Text(
            text = "AUTOMATED AGENT LOGS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MutedSilver,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DeepCharcoal),
            border = BorderStroke(1.dp, CardGray),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("SQLite Logs Cache", fontSize = 11.sp, color = MutedSilver)
                    Text(
                        "Clear Entries",
                        fontSize = 11.sp,
                        color = CyberTeal,
                        modifier = Modifier
                            .clickable { viewModel.clearAllLogs() }
                            .padding(4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    if (agentLogs.isEmpty()) {
                        item {
                            Text(
                                text = "Logs stand ready. Activities performed by Vinayaka AI's scheduler print dynamically here.",
                                fontSize = 12.sp,
                                color = MutedSilver,
                                modifier = Modifier.padding(12.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        items(agentLogs) { log ->
                            AgentLogItem(log = log)
                        }
                    }
                }
            }
        }
    }
}

// Tab 4: Creative & Analytical Studio
@Composable
fun CreativeStudioPane(
    viewModel: VinayakaViewModel,
    storyboards: List<MangaStoryboard>,
    studyPlans: List<StudyPlan>,
    titleInput: String,
    onTitleChange: (String) -> Unit,
    promptInput: String,
    onPromptChange: (String) -> Unit,
    ideaInput: String,
    onIdeaChange: (String) -> Unit,
    drawingPoints: MutableList<Offset>,
    topicInput: String,
    onTopicChange: (String) -> Unit,
    conceptInput: String,
    onConceptChange: (String) -> Unit
) {
    var subTab by remember { mutableStateOf("manga") } // "manga", "study", "sketch"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Sub-Tab Switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DeepCharcoal),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val options = listOf("manga" to "Manga Hub", "study" to "Calc Study", "sketch" to "Sketch Pad")
            options.forEach { (key, label) ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { subTab = key }
                        .background(if (subTab == key) CyberTeal else Color.Transparent)
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (subTab == key) OceanBlack else PureWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Sub Tab Content
        Box(modifier = Modifier.weight(1f)) {
            when (subTab) {
                "manga" -> MangaStoryboardModule(
                    titleInput = titleInput,
                    onTitleChange = onTitleChange,
                    promptInput = promptInput,
                    onPromptChange = onPromptChange,
                    ideaInput = ideaInput,
                    onIdeaChange = onIdeaChange,
                    storyboards = storyboards,
                    onSubmit = {
                        viewModel.executeMangaPipeline(titleInput, promptInput, ideaInput)
                        onTitleChange("")
                        onPromptChange("")
                        onIdeaChange("")
                    },
                    onDelete = { viewModel.deleteStoryboard(it) }
                )
                "study" -> CalculusStudyModule(
                    topicInput = topicInput,
                    onTopicChange = onTopicChange,
                    conceptInput = conceptInput,
                    onConceptChange = onConceptChange,
                    studyPlans = studyPlans,
                    onSubmit = {
                        viewModel.executeCalculusSyllabusStudy("Calculus BC", topicInput, conceptInput)
                        onTopicChange("")
                        onConceptChange("")
                    },
                    onDelete = { viewModel.deleteStudyPlan(it) }
                )
                "sketch" -> InteractiveSketchPad(
                    drawingPoints = drawingPoints,
                    onClear = { drawingPoints.clear() }
                )
            }
        }
    }
}

// SubTab 4A: Manga Modules
@Composable
fun MangaStoryboardModule(
    titleInput: String,
    onTitleChange: (String) -> Unit,
    promptInput: String,
    onPromptChange: (String) -> Unit,
    ideaInput: String,
    onIdeaChange: (String) -> Unit,
    storyboards: List<MangaStoryboard>,
    onSubmit: () -> Unit,
    onDelete: (MangaStoryboard) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = DeepCharcoal),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Draft Manga Character Card", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CyberTeal)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = onTitleChange,
                        label = { Text("Manga Title (e.g. Iron Alchemist)", fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberTeal,
                            unfocusedBorderColor = CardGray,
                            focusedContainerColor = OceanBlack,
                            unfocusedContainerColor = OceanBlack
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("manga_title_input")
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = promptInput,
                        onValueChange = onPromptChange,
                        label = { Text("Character Concept Design (e.g. cyborg-samurai)", fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberTeal,
                            unfocusedBorderColor = CardGray,
                            focusedContainerColor = OceanBlack,
                            unfocusedContainerColor = OceanBlack
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("manga_prompt_input")
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = ideaInput,
                        onValueChange = onIdeaChange,
                        label = { Text("Panel 1 & Scenario description", fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberTeal,
                            unfocusedBorderColor = CardGray,
                            focusedContainerColor = OceanBlack,
                            unfocusedContainerColor = OceanBlack
                        ),
                        modifier = Modifier.fillMaxWidth().height(100.dp).testTag("manga_idea_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onSubmit,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberTeal, contentColor = OceanBlack),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.align(Alignment.End).testTag("manga_submit_button")
                    ) {
                        Text("Add to SQLite Storyboard", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text("STORIES IN DATABASE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MutedSilver, modifier = Modifier.padding(vertical = 6.dp))
        }

        if (storyboards.isEmpty()) {
            item {
                Text(
                    "No story drafts saved yet. Populate fields above to record items in SQLite via Room.",
                    fontSize = 12.sp, color = MutedSilver, modifier = Modifier.padding(12.dp)
                )
            }
        } else {
            items(storyboards) { sb ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    colors = CardDefaults.cardColors(containerColor = DeepCharcoal),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(sb.title, fontWeight = FontWeight.Bold, color = CyberTeal, fontSize = 14.sp)
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete Draft",
                                tint = SystemRed,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable { onDelete(sb) }
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Prompt: ${sb.characterPrompt}", fontSize = 11.sp, color = MutedSilver)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(sb.storyIdea, fontSize = 12.sp, color = PureWhite)
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        // Panel layout summary
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CardGray, RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Text("🎬 Flow Nodes: 3 panel narrative framework set.", fontSize = 10.sp, color = SoftGold)
                        }
                    }
                }
            }
        }
    }
}

// SubTab 4B: Calculus Modules
@Composable
fun CalculusStudyModule(
    topicInput: String,
    onTopicChange: (String) -> Unit,
    conceptInput: String,
    onConceptChange: (String) -> Unit,
    studyPlans: List<StudyPlan>,
    onSubmit: () -> Unit,
    onDelete: (StudyPlan) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = DeepCharcoal),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Calculus Solver & Study Syllabus", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SoftGold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Structuring syllabus explanations, formulas, and mock quizzes.", fontSize = 11.sp, color = MutedSilver)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = topicInput,
                        onValueChange = onTopicChange,
                        label = { Text("Calculus Topic (e.g. Derivatives, Integrals)", fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoftGold,
                            unfocusedBorderColor = CardGray,
                            focusedContainerColor = OceanBlack,
                            unfocusedContainerColor = OceanBlack
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("study_topic_input")
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = conceptInput,
                        onValueChange = onConceptChange,
                        label = { Text("Core Formula / Question parameter", fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoftGold,
                            unfocusedBorderColor = CardGray,
                            focusedContainerColor = OceanBlack,
                            unfocusedContainerColor = OceanBlack
                        ),
                        modifier = Modifier.fillMaxWidth().height(80.dp).testTag("study_concept_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onSubmit,
                        colors = ButtonDefaults.buttonColors(containerColor = SoftGold, contentColor = OceanBlack),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.align(Alignment.End).testTag("study_submit_button")
                    ) {
                        Text("Add study plan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text("STUDY COMPILATIONS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MutedSilver, modifier = Modifier.padding(vertical = 6.dp))
        }

        if (studyPlans.isEmpty()) {
            item {
                Text(
                    "No study cards stored. Use generator above to structure integration/differentiation cards.",
                    fontSize = 12.sp, color = MutedSilver, modifier = Modifier.padding(12.dp)
                )
            }
        } else {
            items(studyPlans) { plan ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    colors = CardDefaults.cardColors(containerColor = DeepCharcoal),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("${plan.subject}: ${plan.topic}", fontWeight = FontWeight.Bold, color = SoftGold, fontSize = 14.sp)
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Remove Plan",
                                tint = SystemRed,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable { onDelete(plan) }
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(plan.explanation, fontSize = 12.sp, color = PureWhite)
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("📝 Simulated Quiz Node:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyberTeal)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CardGray, RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("Q: What is the derivative of x^2?", fontSize = 11.sp, color = PureWhite)
                                Text("A: 2x", fontSize = 11.sp, color = CyberTeal)
                            }
                        }
                    }
                }
            }
        }
    }
}

// SubTab 4C: SketchPad
@Composable
fun InteractiveSketchPad(
    drawingPoints: List<Offset>,
    onClear: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Tactile Character Draft Sketch Board", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CyberTeal)
            Button(
                onClick = onClear,
                colors = ButtonDefaults.buttonColors(containerColor = SystemRed, contentColor = PureWhite),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Text("Reset", fontSize = 10.sp)
            }
        }

        // Draw Canvas board
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(2.dp, CardGray, RoundedCornerShape(12.dp))
                .background(OceanBlack)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val originalPoints = drawingPoints as MutableList<Offset>
                        originalPoints.add(change.position)
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Background grid to feel like a high-tech Blueprint grid!
                val gridGap = 30.dp.toPx()
                for (x in 0..size.width.toInt() step gridGap.toInt()) {
                    drawLine(Color(0xFF141A29), Offset(x.toFloat(), 0f), Offset(x.toFloat(), size.height), strokeWidth = 1f)
                }
                for (y in 0..size.height.toInt() step gridGap.toInt()) {
                    drawLine(Color(0xFF141A29), Offset(0f, y.toFloat()), Offset(size.width, y.toFloat()), strokeWidth = 1f)
                }

                // User traced lines
                for (point in drawingPoints) {
                    drawCircle(
                        color = CyberTeal,
                        radius = 4f,
                        center = point
                    )
                }
            }

            if (drawingPoints.isEmpty()) {
                Text(
                    text = "👇 Trace your finger anywhere here to sketch manga blueprints!",
                    color = MutedSilver,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

// Tab 5: Smart Glasses Simulator Pane
@Composable
fun SmartGlassesVisionPane() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "AMBIENT SMART GLASSES INTERACTION",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = CyberTeal,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Simulated camera vision box
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, CyberTeal, RoundedCornerShape(16.dp))
                .background(DeepCharcoal)
        ) {
            // Simulated HUD drawing inside
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Focus corner markers
                val padding = 30f
                val length = 60f
                val thickness = 4f
                val color = CyberTeal

                // Top Left
                drawRect(color, Offset(padding, padding), Size(length, thickness))
                drawRect(color, Offset(padding, padding), Size(thickness, length))

                // Top Right
                drawRect(color, Offset(size.width - padding - length, padding), Size(length, thickness))
                drawRect(color, Offset(size.width - padding, padding), Size(thickness, length))

                // Bottom Left
                drawRect(color, Offset(padding, size.height - padding), Size(length, thickness))
                drawRect(color, Offset(padding, size.height - padding - length), Size(thickness, length))

                // Bottom Right
                drawRect(color, Offset(size.width - padding - length, size.height - padding), Size(length, thickness))
                drawRect(color, Offset(size.width - padding, size.height - padding - length), Size(thickness, length))

                // Simulated object tracking rect
                drawRoundRect(
                    color = ElectricViolet,
                    topLeft = Offset(size.width / 4, size.height / 3),
                    size = Size(size.width / 2, size.height / 3),
                    cornerRadius = CornerRadius(20f, 20f),
                    style = Stroke(width = 3f)
                )
            }

            // Text Labels overlayed
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("📡 AR GLASSES LINK: ONLINE", fontSize = 11.sp, color = CyberTeal, fontWeight = FontWeight.Bold)
                    Text("LIVE FRAME: 60FPS", fontSize = 11.sp, color = MutedSilver)
                }

                Column(
                    modifier = Modifier
                        .background(OceanBlack.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                        .align(Alignment.CenterHorizontally),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("OBJECT IDENTIFIED", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ElectricViolet)
                    Text("Silicon Neural Interface Accelerator", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                    Text("Probability Score: 98.4% Accuracy", fontSize = 10.sp, color = MutedSilver)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Battery: 88% • Signal: Extreme", fontSize = 10.sp, color = MutedSilver)
                    Text("Scythe Engine V5", fontSize = 10.sp, color = CyberTeal)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DeepCharcoal)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Ambient intelligence Roadmap", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SoftGold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Through upcoming Smart Glass connections, Vinayaka AI transfers from mobile screens into genuine optical layers. Computer Vision algorithms parse physical objects, while augmented overlays provide immediate context-rich summaries directly to your biological viewpoint.",
                    fontSize = 11.sp,
                    color = MutedSilver
                )
            }
        }
    }
}

// ------------------- AUX COMPOSABLES & VIEW ITEMS -------------------

@Composable
fun AvatarCanvasRenderer(
    emotionState: String,
    micPower: Float
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val baseRadius = size.width * 0.35f * pulseScale

        // Draw cybernetic outer radial rings
        drawCircle(
            color = when (emotionState) {
                "Thinking" -> ElectricViolet.copy(alpha = 0.2f)
                "Happy" -> CyberTeal.copy(alpha = 0.2f)
                "Encouraging" -> SoftGold.copy(alpha = 0.2f)
                else -> GlowGreen.copy(alpha = 0.2f)
            },
            radius = baseRadius + 30f,
            style = Stroke(width = 4f)
        )

        drawCircle(
            color = when (emotionState) {
                "Thinking" -> ElectricViolet.copy(alpha = 0.6f)
                "Happy" -> CyberTeal.copy(alpha = 0.6f)
                "Encouraging" -> SoftGold.copy(alpha = 0.6f)
                else -> GlowGreen.copy(alpha = 0.6f)
            },
            radius = baseRadius,
            style = Stroke(width = 6f)
        )

        // Dynamic State drawings inside
        when (emotionState) {
            "Thinking" -> {
                // Draw concentration gears inside
                val angle = (System.currentTimeMillis() / 20) % 360
                drawArc(
                    color = ElectricViolet,
                    startAngle = angle.toFloat(),
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(centerX - 40f, centerY - 40f),
                    size = Size(80f, 80f),
                    style = Stroke(width = 8f)
                )
                // Thinking lines
                drawLine(
                    color = ElectricViolet,
                    start = Offset(centerX - 60f, centerY),
                    end = Offset(centerX + 60f, centerY),
                    strokeWidth = 4f
                )
            }
            "Happy" -> {
                // Smile shape
                drawArc(
                    color = CyberTeal,
                    startAngle = 10f,
                    sweepAngle = 160f,
                    useCenter = false,
                    topLeft = Offset(centerX - 50f, centerY - 30f),
                    size = Size(100f, 80f),
                    style = Stroke(width = 6f)
                )
                // Happy eye dots
                drawCircle(CyberTeal, 8f, Offset(centerX - 30f, centerY - 20f))
                drawCircle(CyberTeal, 8f, Offset(centerX + 30f, centerY - 20f))
            }
            "Encouraging" -> {
                // Heart or warm star symbol inside
                drawCircle(SoftGold, 30f, Offset(centerX, centerY))
                drawCircle(SoftGold.copy(alpha = 0.5f), 55f + (micPower * 30f), Offset(centerX, centerY))
            }
            else -> {
                // Neutral visor
                drawRoundRect(
                    color = GlowGreen,
                    topLeft = Offset(centerX - 60f, centerY - 15f),
                    size = Size(120f, 30f),
                    cornerRadius = CornerRadius(15f, 15f),
                    style = Stroke(width = 4f)
                )
                drawRect(
                    color = GlowGreen,
                    topLeft = Offset(centerX - 35f, centerY - 4f),
                    size = Size(70f, 8f)
                )
            }
        }
    }
}

@Composable
fun SpeechSoundwaveVisualizer(
    speechType: String,
    micLevel: Float
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (speechType == "Listening") "👂 Listening for voice parameters..." else "🔊 Speaking...",
            fontSize = 11.sp,
            color = if (speechType == "Listening") CyberTeal else ElectricViolet,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Row(
            modifier = Modifier
                .height(40.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val count = 12
            for (i in 0 until count) {
                // Math wave multiplication
                val amplitudeFactor = sin(i.toFloat() / count * Math.PI).toFloat()
                val randomJitter = (micLevel * 35.dp.value)
                val lineH = (5.dp.value + (randomJitter * amplitudeFactor)).dp

                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .width(4.dp)
                        .height(lineH)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (speechType == "Listening") CyberTeal else ElectricViolet)
                )
            }
        }
    }
}

@Composable
fun ChatBubble(chat: CompanionChat, onSpeak: () -> Unit) {
    val alignEnd = chat.isUser
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalArrangement = if (alignEnd) Arrangement.End else Arrangement.Start
        ) {
            if (!alignEnd) {
                // Companion avatar representation bubble
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            when (chat.emotionState) {
                                "Thinking" -> ElectricViolet
                                "Happy" -> CyberTeal
                                "Encouraging" -> SoftGold
                                else -> GlowGreen
                            }
                        )
                        .clickable { onSpeak() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "Read out loud",
                        tint = OceanBlack,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Message Bubble box
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (alignEnd) CyberTeal else DeepCharcoal
                ),
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (alignEnd) 16.dp else 2.dp,
                    bottomEnd = if (alignEnd) 2.dp else 16.dp
                ),
                modifier = Modifier.shadow(2.dp, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = chat.text,
                        color = if (alignEnd) OceanBlack else PureWhite,
                        fontSize = 14.sp,
                        fontWeight = if (alignEnd) FontWeight.SemiBold else FontWeight.Normal
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(chat.timestamp)),
                        fontSize = 10.sp,
                        color = if (alignEnd) OceanBlack.copy(alpha = 0.6f) else MutedSilver,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}

@Composable
fun AgentLogItem(log: AgentLog) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(Color(0xFF131726), RoundedCornerShape(6.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (log.status == "Completed") GlowGreen else ElectricViolet)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(log.actionName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PureWhite)
            Text(log.details, fontSize = 11.sp, color = MutedSilver)
        }
    }
}

@Composable
fun ThinkingIndicator() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(ElectricViolet),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.HourglassEmpty, contentDescription = "Thinking", tint = OceanBlack, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = DeepCharcoal),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = ElectricViolet)
                Spacer(modifier = Modifier.width(10.dp))
                Text("AI Orchestrator matching model weight cores...", fontSize = 12.sp, color = MutedSilver)
            }
        }
    }
}

@Composable
fun EmptyStatePlaceholder(title: String, desc: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 28.dp),
        colors = CardDefaults.cardColors(containerColor = DeepCharcoal.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = "Active", tint = CyberTeal, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PureWhite, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(6.dp))
            Text(desc, fontSize = 12.sp, color = MutedSilver, textAlign = TextAlign.Center)
        }
    }
}

// Full screen overlay simulator for Ambient Lock Screen
@Composable
fun LockScreenSimulator(viewModel: VinayakaViewModel) {
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY).toString().padStart(2, '0')
    val minute = calendar.get(Calendar.MINUTE).toString().padStart(2, '0')

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OceanBlack)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Exit simulator button
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("🔒 AMBIENT LOCK SIMULATOR", fontSize = 11.sp, color = CyberTeal, fontWeight = FontWeight.Bold)
            Button(
                onClick = { viewModel.showAmbientLockScreen = false },
                colors = ButtonDefaults.buttonColors(containerColor = CardGray, contentColor = PureWhite),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Text("Dismiss Overlay", fontSize = 11.sp)
            }
        }

        // Holographic Clock
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$hour:$minute",
                fontSize = 80.sp,
                fontWeight = FontWeight.Black,
                color = PureWhite,
                letterSpacing = (-2).sp
            )
            Text(
                text = "Tuesday, June 9",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MutedSilver
            )
        }

        // Pulse Orb representation of Vinayaka AI standing ready in background
        Box(
            modifier = Modifier
                .size(120.dp)
                .clickable {
                    viewModel.showAmbientLockScreen = false
                    viewModel.triggerVoiceWake()
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = CyberTeal.copy(alpha = 0.15f),
                    radius = size.width / 2
                )
                drawCircle(
                    color = CyberTeal,
                    radius = size.width / 4,
                    style = Stroke(width = 4f)
                )
            }
            Icon(Icons.Default.Mic, contentDescription = "Voice Activation", tint = CyberTeal, modifier = Modifier.size(32.dp))
        }

        // Ambient notifications & quick briefings
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DeepCharcoal),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("📅 Daily Briefing Node", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SoftGold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Syllabus calculus study cards are completely synced. 3 Manga story drafts preserved. Hardware storage remains 94% optimized.",
                    fontSize = 12.sp,
                    color = MutedSilver
                )
            }
        }
    }
}
