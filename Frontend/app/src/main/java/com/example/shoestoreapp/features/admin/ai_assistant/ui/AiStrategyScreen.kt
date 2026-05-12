package com.example.shoestoreapp.features.admin.ai_assistant.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shoestoreapp.features.agent_intelligent.data.remote.ChatSessionResponseDto
import com.example.shoestoreapp.features.admin.ai_assistant.viewmodel.AiStrategyViewmodel
import dev.jeziellago.compose.markdowntext.MarkdownText

/**
 * AI Strategy Assistant Screen
 * UI Rendered purely from ViewModel's State with Typing Effect & Auto-Scroll
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiStrategyScreen(
    viewModel: AiStrategyViewmodel,
    initialPrompt: String? = null,
    onBackClick: () -> Unit = {}
) {

    val state = viewModel.state

    // Save text of users when users are typing
    var inputText by remember { mutableStateOf("") }
    var showHistorySheet by remember { mutableStateOf(false) }

    // Manage scroll state  of list
    val listState = rememberLazyListState()

    LaunchedEffect(initialPrompt) {
        viewModel.initialize(initialPrompt)
    }

    // Auto scroll when AI response
    val messages = state.messages
    LaunchedEffect(messages.size, messages.lastOrNull()?.text?.length) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                onBackClick = onBackClick,
                onHistoryClick = { showHistorySheet = true },
                viewModel = viewModel
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                if (!state.error.isNullOrBlank()) {
                    item {
                        Text(
                            text = state.error,
                            color = Color(0xFFB00020),
                            fontSize = 12.sp
                        )
                    }
                }
                if (state.messages.isEmpty() && !initialPrompt.isNullOrBlank()) {
                    item { UserMessageBubble(text = initialPrompt) }
                    item { AiMessageBubble(text = "Analyzing... ", isStreaming = true) }
                } else if (state.messages.isEmpty()) {
                    item {
                        Text(
                            text = "Please enter a new question to start",
                            fontSize = 13.sp,
                            color = Color(0xFF999999)
                        )
                    }
                } else {
                    items(state.messages) { message ->
                        if (message.isUser) {
                            UserMessageBubble(text = message.text)
                        } else {
                            AiMessageBubble(text = message.text, isStreaming = message.isStreaming)
                        }
                    }
                }
            }

            // Input Require
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .imePadding() // Push up when keyboard appears
                    .padding(16.dp)
            ) {
                ChatInputBar(
                    value = inputText,
                    onValueChange = { inputText = it },
                    onSend = {
                        viewModel.SendMessage(inputText)
                        inputText = ""
                    }
                )
            }
        }
    }

    if (showHistorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showHistorySheet = false },
            containerColor = Color.White
        ) {
            SessionListArea(
                sessions = state.sessions,
                isLoading = state.isLoadingSesions,
                onSessionClick = { sessionId ->
                    viewModel.selectSession(sessionId)
                    showHistorySheet = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .padding(bottom = 12.dp)
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar (
    onBackClick: () -> Unit,
    onHistoryClick : () -> Unit,
    viewModel: AiStrategyViewmodel
){
        CenterAlignedTopAppBar(
            title = { Text("Strategy Assistant", fontSize = 24.sp, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                // History Sessions Button
                IconButton(onClick = {
                    onHistoryClick()
                    viewModel.loadSessions()
                }) {
                    Icon(Icons.Default.History, contentDescription = "History", tint = Color.Gray)
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
        )
}
/**
 * Old Sessions Area
 */
@Composable
fun SessionListArea(
    sessions: List<ChatSessionResponseDto>,
    isLoading: Boolean,
    onSessionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color.Black)
        }
        return
    }

    LazyColumn(modifier = modifier.padding(16.dp)) {
        item {
            Text("Recent Strategies", fontSize = 14.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(bottom = 16.dp))
        }
        itemsIndexed(sessions) { index, session ->
            val displayTitle = when {
                !session.title.isNullOrBlank() -> session.title
                session.publicId.isNotBlank() -> "Session: ${session.publicId.take(8)}..."
                else -> "Session #${index + 1}"
            }
            val sessionId = session.publicId
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clickable(enabled = sessionId.isNotBlank()) {
                        if (sessionId.isNotBlank()) {
                            onSessionClick(sessionId)
                        }
                    },
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F4F5))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Insights, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = displayTitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Message Bubble of AI
 */
@Composable
fun AiMessageBubble(text: String, isStreaming: Boolean = false) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Color(0xFFF4F4F5), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.SupportAgent, contentDescription = null, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .background(Color(0xFFF4F4F5), RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp))
                    .padding(12.dp)
            ) {
                if (isStreaming && text.isBlank()) {
                    ThinkingIndicator()
                } else {
                    val displayText = if (isStreaming) "$text |" else text
                    MarkdownText(
                        markdown = displayText,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = Color(0xFF27272A),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Text(
                "AI STRATEGIST • NOW",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.LightGray,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
        Spacer(modifier = Modifier.width(40.dp))
    }
}

@Composable
private fun ThinkingIndicator() {
    val transition = rememberInfiniteTransition(label = "thinking")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "thinkingAlpha"
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            strokeWidth = 2.dp,
            color = Color.Black
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Thinking ...",
            fontSize = 13.sp,
            color = Color(0xFF27272A),
            modifier = Modifier.alpha(alpha)
        )
    }
}

/**
 * Message Bubble of Admin (User)
 */
@Composable
fun UserMessageBubble(text: String) {
    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.width(60.dp))
        Column(horizontalAlignment = Alignment.End) {
            Box(
                modifier = Modifier
                    .background(Color.Black, RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp))
                    .padding(12.dp)
            ) {
                Text(text = text, fontSize = 14.sp, color = Color.White, lineHeight = 20.sp)
            }
            Text(
                "ADMIN • JUST NOW",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.LightGray,
                modifier = Modifier.padding(top = 4.dp, end = 4.dp)
            )
        }
    }
}

/**
 * Input bar bottom phone
 */
@Composable
fun ChatInputBar(value: String, onValueChange: (String) -> Unit, onSend: () -> Unit) {
    Surface(
        tonalElevation = 8.dp,
        shadowElevation = 12.dp,
        shape = RoundedCornerShape(24.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) Text("Ask about optimization...", color = Color.LightGray, fontSize = 14.sp)
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            IconButton(
                onClick = onSend,
                modifier = Modifier.background(Color.Black, CircleShape).size(40.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}