package com.example.shoestoreapp.features.agent_intelligent.ui

import android.app.Activity
import android.os.Build
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shoestoreapp.features.agent_intelligent.data.remote.ChatSessionResponseDto
import com.example.shoestoreapp.features.agent_intelligent.viewmodel.BaseAIViewModel
import dev.jeziellago.compose.markdowntext.MarkdownText
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

data class AiQuickAction(
    val label: String,
    val prompt: String,
    val icon: ImageVector
)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseAIChatScreen(
    viewModel: BaseAIViewModel,
    title: String,
    initialPrompt: String? = null,
    userRoleName: String,
    aiRoleName: String,
    onBackClick: () -> Unit = {},
    headerContent: (@Composable () -> Unit)? = null,
    footerContent: (@Composable () -> Unit)? = null,
    bottomBarContent: (@Composable () -> Unit)? = null,
    emptyTitle: String = "Hello! How can I help you?",
    emptySubtitle: String = "Type a question or choose a suggestion below to get started.",
    inputPlaceholder: String = "Ask me anything...",
    quickActions: List<AiQuickAction> = emptyList(),
    onQuickActionClick: (AiQuickAction) -> Unit = { action -> viewModel.SendMessage(action.prompt) },
) {
    val state = viewModel.state
    val context = LocalContext.current
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current

    var inputText by remember { mutableStateOf("") }
    var showHistorySheet by remember { mutableStateOf(false) }
    var shouldScrollToSentUserMessage by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    LaunchedEffect(initialPrompt) {
        viewModel.initialize(initialPrompt)
    }

    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        val previousSoftInputMode = window?.attributes?.softInputMode
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        onDispose {
            previousSoftInputMode?.let { window.setSoftInputMode(it) }
        }
    }

    val messages = state.messages
    val hasFooter = footerContent != null
    val isKeyboardVisible = WindowInsets.ime.getBottom(density) > 0
    val lastUserMessageId = messages.lastOrNull { it.isUser }?.id

    LaunchedEffect(lastUserMessageId, shouldScrollToSentUserMessage, state.error, headerContent) {
        if (!shouldScrollToSentUserMessage || lastUserMessageId == null) return@LaunchedEffect

        withFrameNanos { }
        val sentMessageIndex = messages.indexOfLast { it.id == lastUserMessageId }
        if (sentMessageIndex >= 0) {
            val messageStartIndex =
                (if (!state.error.isNullOrBlank()) 1 else 0) +
                    (if (headerContent != null) 1 else 0)
            listState.scrollToItem(messageStartIndex + sentMessageIndex)
        }
        shouldScrollToSentUserMessage = false
    }
    Scaffold(
        topBar = {
            SharedChatTopBar(
                titleTopBar = title,
                onBackClick = onBackClick,
                onHistoryClick = { showHistorySheet = true },
                onLoadSessions = { viewModel.loadSessions(isNextPage = false) })
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .imePadding()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp, bottom = 8.dp)
                ) {
                    SharedChatInputBar(
                        value = inputText,
                        onValueChange = { inputText = it },
                        onSend = {
                            shouldScrollToSentUserMessage = true
                            viewModel.SendMessage(inputText)
                            inputText = "" // clear input aften sending
                        },
                        placeholder = inputPlaceholder
                    )
                }

                if (!isKeyboardVisible) {
                    bottomBarContent?.invoke()
                }
            }
        },
        containerColor = Color.White
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = paddingValues.calculateStartPadding(layoutDirection),
                    top = paddingValues.calculateTopPadding(),
                    end = paddingValues.calculateEndPadding(layoutDirection),
                    bottom = paddingValues.calculateBottomPadding()
                )
                .background(Color.White)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                if (!state.error.isNullOrBlank()) {
                    item {
                        Text(
                            text = state.error, color = Color(0xFFB00020), fontSize = 12.sp
                        )
                    }
                }
                headerContent?.let {
                    item {
                        it()
                    }
                }
                if (state.messages.isEmpty() && !initialPrompt.isNullOrBlank()) {
                    item { SharedUserMessageBubble(text = initialPrompt, roleName = userRoleName) }
                    item {
                        SharedAiMessageBubble(
                            text = "Analyzing... ", isStreaming = true, roleName = aiRoleName
                        )
                    }
                } else if (state.messages.isEmpty()) {
                    item {
                        SharedAiEmptyState(
                            title = emptyTitle,
                            subtitle = emptySubtitle,
                        quickActions = quickActions,
                        onQuickActionClick = { action ->
                            shouldScrollToSentUserMessage = true
                            onQuickActionClick(action)
                        }
                    )
                }
                } else {
                    items(state.messages) { message ->
                        if (message.isUser) {
                            SharedUserMessageBubble(
                                text = message.text,
                                roleName = userRoleName,
                                timeString =  formatChatTime(message.createdAt)
                            )
                        } else {
                            SharedAiMessageBubble(
                                text = message.displayText,
                                isStreaming = message.isStreaming,
                                roleName = aiRoleName,
                                timeString = if (message.isStreaming) "Now" else formatChatTime(message.createdAt)
                            )
                        }
                    }
                }
                footerContent?.let {
                    item {
                        it()
                    }
                }
            }

        }
    }

    if (showHistorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showHistorySheet = false }, containerColor = Color.White
        ) {
            SharedSessionListArea(
                sessions = state.sessions,
                isLoading = state.isLoadingSesions,
                isMoreLoading = state.isMoreLoading,
                onSessionClick = { sessionId ->
                    viewModel.selectSession(sessionId)
                    showHistorySheet = false
                },
                onLoadMore = { viewModel.loadSessions(isNextPage = true) },
                onClearAll = { viewModel.clearAllSessions() },
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
fun SharedChatTopBar(
    titleTopBar: String,
    onBackClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onLoadSessions: () -> Unit,
) {
    CenterAlignedTopAppBar(
        title = { Text(titleTopBar, fontSize = 22.sp, fontWeight = FontWeight.Black) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            // History Sessions Button
            IconButton(onClick = {
                onHistoryClick()
                onLoadSessions()
            }) {
                Icon(Icons.Default.History, contentDescription = "History", tint = Color.Gray)
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
    )
}

@Composable
fun SharedChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    placeholder: String
) {
    Surface(
        tonalElevation = 8.dp,
        shadowElevation = 12.dp,
        shape = RoundedCornerShape(28.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AttachFile,
                contentDescription = "Attach",
                tint = Color(0xFF555555),
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) Text(
                    placeholder, color = Color(0xFFC7C7C7), fontSize = 16.sp
                )
                BasicTextField(
                    value = value, onValueChange = onValueChange, modifier = Modifier.fillMaxWidth()
                )
            }
            IconButton(
                onClick = onSend,
                modifier = Modifier
                    .background(Color.Black, CircleShape)
                    .size(42.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun SharedAiEmptyState(
    title: String,
    subtitle: String,
    quickActions: List<AiQuickAction>,
    onQuickActionClick: (AiQuickAction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(88.dp),
            shape = CircleShape,
            color = Color.White,
            tonalElevation = 3.dp,
            shadowElevation = 10.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI",
                    tint = Color.Black,
                    modifier = Modifier.size(38.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(88.dp))
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = subtitle,
            fontSize = 20.sp,
            lineHeight = 30.sp,
            color = Color(0xFF555555),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            quickActions.forEach { action ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, Color.Black, RoundedCornerShape(12.dp))
                        .clickable { onQuickActionClick(action) }
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(18.dp))
                    Text(
                        text = action.label,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun SharedSessionListArea(
    sessions: List<ChatSessionResponseDto>,
    isLoading: Boolean,
    isMoreLoading: Boolean,
    onSessionClick: (String) -> Unit,
    onLoadMore: () -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    var searchText by remember { mutableStateOf("") }
    val filteredSessions = remember(sessions, searchText) {
        if (searchText.isBlank()) sessions
        else sessions.filter { session ->
            session.title.orEmpty().contains(searchText, ignoreCase = true) ||
                session.publicId.orEmpty().contains(searchText, ignoreCase = true)
        }
    }

    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem != null && lastVisibleItem.index >= filteredSessions.size - 3
        }
    }
    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && !isLoading && !isMoreLoading) {
            onLoadMore()
        }
    }
    // Wait loading sessions history
    if (isLoading && sessions.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color.Black)
        }
        return
    }

    LazyColumn(
        state = listState, // Attach a camera to track swipe gestures
        modifier = modifier.padding(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Conversation History",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onClearAll) {
                    Text("Clear all", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFFAFAFA),
                tonalElevation = 0.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE8E8E8))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF999999))
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchText.isBlank()) {
                            Text("Search conversations...", color = Color(0xFFD7D7D7), fontSize = 16.sp)
                        }
                        BasicTextField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        itemsIndexed(filteredSessions) { index, session ->
            val sessionId = session.publicId ?: ""
            val displayTitle = when {
                !session.title.isNullOrBlank() -> session.title
                session.publicId.isNullOrBlank() -> "Session: ${sessionId.take(8)}..."
                else -> "Session #${index + 1}"
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clickable(enabled = sessionId.isNotBlank()) {
                        onSessionClick(sessionId)
                    }, colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F4F5))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Insights,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = displayTitle, fontSize = 14.sp, fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        if (isMoreLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color.Black, modifier = Modifier.size(24.dp)
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
fun SharedAiMessageBubble(
    text: String,
    isStreaming: Boolean = false,
    roleName: String,
    timeString: String? = null
) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Color(0xFFF4F4F5), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.SupportAgent,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .background(
                        Color(0xFFF4F4F5), RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp)
                    )
                    .padding(12.dp)
            ) {
                if (isStreaming && text.isBlank()) {
                    SharedThinkingIndicator()
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
                text = "$roleName • $timeString",
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
fun SharedUserMessageBubble(
    text: String,
    roleName: String,
    timeString: String? = null
) {
    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.width(60.dp))
        Column(horizontalAlignment = Alignment.End) {
            Box(
                modifier = Modifier
                    .background(
                        Color.Black, RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp)
                    )
                    .padding(12.dp)
            ) {
                Text(text = text, fontSize = 14.sp, color = Color.White, lineHeight = 20.sp)
            }
            Text(
                text = "$roleName • $timeString",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.LightGray,
                modifier = Modifier.padding(top = 4.dp, end = 4.dp)
            )
        }
    }
}

@Composable
private fun SharedThinkingIndicator() {
    val transition = rememberInfiniteTransition(label = "thinking")
    val alpha by transition.animateFloat(
        initialValue = 0.3f, targetValue = 1f, animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "thinkingAlpha"
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        CircularProgressIndicator(
            modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.Black
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Thinking...",
            fontSize = 13.sp,
            color = Color(0xFF27272A),
            modifier = Modifier.alpha(alpha)
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun formatChatTime(timeStr: String?): String {
    if (timeStr.isNullOrBlank()) return "Now"

    return runCatching {

        val parsedTime = ZonedDateTime.parse(timeStr)
        //Convert to the device's local time zone
        val localTime = parsedTime.withZoneSameInstant(ZoneId.systemDefault())

        //Format to the desired pattern: Day/Month/Year Hour:Minute
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        localTime.format(formatter)
    }.getOrDefault("Now") // Fallback to "Now" if any parsing exception occurs
}
