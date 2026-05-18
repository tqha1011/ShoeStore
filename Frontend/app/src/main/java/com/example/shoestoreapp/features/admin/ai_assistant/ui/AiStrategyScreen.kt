package com.example.shoestoreapp.features.admin.ai_assistant.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.shoestoreapp.features.admin.ai_assistant.viewmodel.AiStrategyViewmodel
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
    onBackClick: () -> Unit = {}
)  {
    BaseAIChatScreen(
        viewModel = viewModel,
        title = "STRATERY ASSISTANT",
        initialPrompt = initialPrompt,
        userRoleName = "Admin",
        aiRoleName = "Ai Strategy",
        onBackClick = onBackClick,
    )
}
