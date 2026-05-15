package com.example.shoestoreapp.features.user.ai_assistant.viewmodel

import com.example.shoestoreapp.features.agent_intelligent.data.repository.AiChatRepository
import com.example.shoestoreapp.features.agent_intelligent.viewmodel.BaseAIViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow



class AiProductViewmodel(
    repository: AiChatRepository,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO
): BaseAIViewModel(repository, ioDispatcher) {

    override fun getStreamFlow(sessionId: String, prompt: String, isCampaign: Boolean): Flow<String> {
        return repository.streamChatProducts(sessionId, prompt)
    }
    
}