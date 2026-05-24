package com.example.shoestoreapp.features.admin.ai_assistant.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestoreapp.core.utils.SignalRManager
import com.example.shoestoreapp.features.agent_intelligent.data.remote.ChatSessionResponseDto
import com.example.shoestoreapp.features.agent_intelligent.data.repository.AiChatRepository
import com.example.shoestoreapp.features.agent_intelligent.viewmodel.BaseAIViewModel
import com.example.shoestoreapp.features.agent_intelligent.viewmodel.ChatMessage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AiStrategyViewmodel(
     repository : AiChatRepository,
     signalRManager: SignalRManager,
     ioDispatcher : CoroutineDispatcher = Dispatchers.IO,
) : BaseAIViewModel(repository, signalRManager, ioDispatcher) {

    override fun initialize(initialPrompt: String?) {
        // For strategy, we might want to load the most recent session or start fresh
        if (!initialPrompt.isNullOrBlank()){
            SendMessage(userText = initialPrompt, isCampaign = true)
        }
    }
    override fun getStreamFlow(sessionId: String, prompt: String, isCampaign: Boolean): Flow<String> {
        return if (isCampaign) {
            repository.streamGenerateCampaign(sessionId, prompt)
        }
        else {
            repository.streamChatStatistics(sessionId, prompt)
        }
    }
}