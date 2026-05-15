package com.example.shoestoreapp.features.user.ai_assistant.ui

import androidx.compose.runtime.Composable
import com.example.shoestoreapp.features.agent_intelligent.ui.BaseAIChatScreen
import com.example.shoestoreapp.features.user.ai_assistant.viewmodel.AiProductViewmodel

@Composable
fun AiProductScreen(
    viewModel: AiProductViewmodel,
    initialPrompt: String? = null,
    onBackClick: () -> Unit = {}
) {
    BaseAIChatScreen(
        viewModel = viewModel,
        title = "Product Assistant",
        aiRoleName = "AI ASSISTANT",
        userRoleName = "USER",
        initialPrompt = initialPrompt,
        onBackClick = onBackClick
    )
}