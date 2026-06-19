package com.example.shoestoreapp.features.admin.ai_assistant.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.shoestoreapp.features.admin.ai_assistant.viewmodel.AiStrategyViewmodel
import com.example.shoestoreapp.features.agent_intelligent.ui.AiQuickAction
import com.example.shoestoreapp.features.agent_intelligent.ui.BaseAIChatScreen

/**
 * AI Strategy Assistant Screen
 * UI Rendered purely from ViewModel's State with Typing Effect & Auto-Scroll
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiStrategyScreen(
    viewModel: AiStrategyViewmodel,
    initialPrompt: String? = null,
    onBackClick: () -> Unit = {},
    title: String = "AI Assistant",
    headerContent: (@Composable () -> Unit)? = null,
    bottomBarContent: (@Composable () -> Unit)? = null
)  {
    BaseAIChatScreen(
        viewModel = viewModel,
        title = title,
        initialPrompt = initialPrompt,
        userRoleName = "Admin",
        aiRoleName = "AI Analytics",
        onBackClick = onBackClick,
        headerContent = headerContent,
        bottomBarContent = bottomBarContent,
        emptyTitle = "Hello! How can I help you?",
        emptySubtitle = "Type a question or choose a suggestion below to get started.",
        inputPlaceholder = "Ask me anything...",
        quickActions = listOf(
            AiQuickAction(
                label = "Analyze this month's revenue",
                prompt = "Analyze this month's revenue and summarize the most important insights.",
                icon = Icons.Default.Insights
            ),
            AiQuickAction(
                label = "Find growth opportunities",
                prompt = "Find growth opportunities from current shop statistics.",
                icon = Icons.Default.TrendingUp
            ),
            AiQuickAction(
                label = "Create a marketing campaign",
                prompt = "Generate a marketing campaign to improve shop revenue based on shop statistics.",
                icon = Icons.Default.Campaign
            )
        ),
        onQuickActionClick = { action ->
            val isCampaign = action.label.contains("campaign", ignoreCase = true)
            viewModel.SendMessage(action.prompt, isCampaign = isCampaign)
        }
    )
}
