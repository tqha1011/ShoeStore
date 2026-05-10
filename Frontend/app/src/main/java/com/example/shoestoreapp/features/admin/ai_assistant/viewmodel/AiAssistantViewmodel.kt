package com.example.shoestoreapp.features.admin.ai_assistant.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestoreapp.features.admin.ai_assistant.data.remote.ChatSessionResponseDto
import com.example.shoestoreapp.features.admin.ai_assistant.data.repository.AiChatRepository
import com.example.shoestoreapp.features.admin.ai_assistant.viewmodel.ChatMessage
import com.example.shoestoreapp.features.auth.presentation.reset_password.create_new_password.ErrorDisplay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class AiAssistantState(
    val messages: List<ChatMessage> = emptyList(),
    val sessions: List<ChatSessionResponseDto> = emptyList(),
    val isLoadingSesions : Boolean = false,
    val currentSessionId : String? = null,
    val error : String? = null
)

class AiAssistantViewmodel(
    private val repository : AiChatRepository
) : ViewModel() {
    var state by mutableStateOf(AiAssistantState())
        private set

    fun initialize(initialPrompt: String?) {
        if (!initialPrompt.isNullOrBlank()) {
            // Generate Campaign
            startNewSessionAndGenerateCampaign(initialPrompt)
        } else {
            // Ai Icons
            loadSessions()
        }
    }

    fun loadSessions() {
        viewModelScope.launch(Dispatchers.IO){
            state = state.copy(isLoadingSesions = true, error = null)

            // Get Api Session History
            runCatching {
                val response = repository.getSessions(1, 10)
                if (response.isSuccessful) response.body() ?.items ?: emptyList()
                else throw Exception("Failed to load history: ${response.code()}")
            }
                .onSuccess { items ->
                    state = state.copy(sessions = items, isLoadingSesions = false)
                }
                .onFailure { exception ->
                    state = state.copy(error = exception.message, isLoadingSesions = false)
                }
        }
    }
    private fun startNewSessionAndGenerateCampaign(initialPrompt: String) {
        viewModelScope.launch(Dispatchers.IO){
            runCatching {
                // Create new session
                val response = repository.createSession()
                if (response.isSuccessful) response.body()?.publicSessionId
                else throw Exception("Failed to create session: ${response.code()}")
            }
                .onSuccess { sessionId ->
                    if (sessionId != null) {
                        // Save ID session into state
                        state = state.copy(currentSessionId = sessionId, error = null)
                        // Have ID , start stream letter of AI
                        streamResponse(initialPrompt, isCampaign = true)
                    }
                    else {
                        state = state.copy(error = "Session creation returned null ID")
                    }
                }
                .onFailure { exception ->
                    val errorMsg = ChatMessage(text = "Network error : ${exception.message}", isUser = false)
                    state = state.copy(messages = state.messages + errorMsg, error = exception.message)
                }
        }
    }
    fun SendMessage(userText : String) {
        if (userText.isBlank()) return

        val sessionId = state.currentSessionId
        if (sessionId == null) {
            startNewSessionAndSendMessage(userText)
            return
        }

        streamResponse(userText, isCampaign = false)
    }

    private fun startNewSessionAndSendMessage(userText: String) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val response = repository.createSession()
                if (response.isSuccessful) response.body()?.publicSessionId
                else throw Exception("Failed to create session: ${response.code()}")
            }
                .onSuccess { sessionId ->
                    if (sessionId != null) {
                        state = state.copy(currentSessionId = sessionId, error = null)
                        streamResponse(userText, isCampaign = false)
                    } else {
                        state = state.copy(error = "Session creation returned null ID")
                    }
                }
                .onFailure { exception ->
                    val errorMsg = ChatMessage(text = "Network error : ${exception.message}", isUser = false)
                    state = state.copy(messages = state.messages + errorMsg, error = exception.message)
                }
        }
    }

    fun streamResponse(prompt : String, isCampaign : Boolean){
        val sessionId = state.currentSessionId ?: return

        // Create Bubble Chat for user messages
        val userMsg = ChatMessage(text = prompt, isUser = true)

        // Create Bubble Chat for AI messages
        val aiMessageId = java.util.UUID.randomUUID().toString()
        val aiMsgPlaceHolder = ChatMessage(id = aiMessageId, text = "", isUser = false, isStreaming = true)

        state = state.copy(messages = state.messages + userMsg + aiMsgPlaceHolder, error = null)

        viewModelScope.launch(Dispatchers.IO){

            val streamFlow = if (isCampaign) {
                repository.streamGenerateCampaign(sessionId, prompt)
            } else {
                repository.streamChatStatistics(sessionId, prompt)
            }

            streamFlow
                .catch { exception ->
                    val updatedMessages = state.messages.map { msg ->
                        if (msg.id == aiMessageId) {
                            msg.copy(text = "Error Response: ${exception.message}", isStreaming = false)
                        }
                        else msg
                    }
                    state = state.copy(messages = updatedMessages)
        }
                .collect { chunk ->
                    val formattedChunk = chunk.replace("\\n", "\n")

                    val updatedMessages = state.messages.map { msg ->
                        if (msg.id == aiMessageId){
                            msg.copy(text = msg.text + formattedChunk)
                        }
                        else msg
                    }
                    state = state.copy(messages = updatedMessages)
                }
            val finalMessage = state.messages.map { msg ->
                if (msg.id == aiMessageId) msg.copy(isStreaming = false)
                else msg
            }
            state = state.copy(messages = finalMessage)
        }
    }
    fun clearError() {
        state = state.copy(error = null)
    }
}