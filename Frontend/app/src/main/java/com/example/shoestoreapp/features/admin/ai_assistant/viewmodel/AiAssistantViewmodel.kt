package com.example.shoestoreapp.features.admin.ai_assistant.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestoreapp.features.admin.ai_assistant.data.remote.ChatSessionResponseDto
import com.example.shoestoreapp.features.admin.ai_assistant.data.repository.AiChatRepository
import com.example.shoestoreapp.features.admin.ai_assistant.viewmodel.ChatMessage
import com.example.shoestoreapp.features.auth.presentation.reset_password.create_new_password.ErrorDisplay
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AiAssistantState(
    val messages: List<ChatMessage> = emptyList(),
    val sessions: List<ChatSessionResponseDto> = emptyList(),
    val isLoadingSesions : Boolean = false,
    val currentSessionId : String? = null,
    val error : String? = null
)

class AiAssistantViewmodel(
    private val repository : AiChatRepository,
    private val ioDispatcher : CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    var state by mutableStateOf(AiAssistantState())
        private set

    fun initialize(initialPrompt: String?) {
        if (!initialPrompt.isNullOrBlank()) {
            // Generate Campaign
            startNewSessionAndGenerateCampaign(initialPrompt)
        }
    }

    fun loadSessions() {
        viewModelScope.launch(ioDispatcher){
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
        viewModelScope.launch(ioDispatcher){
            runCatching {
                // Create new session
                val response = repository.createSession()
                if (!response.isSuccessful) {
                    val errorBody = response.errorBody()?.string()?.take(200)
                    throw Exception("Failed to create session: ${response.code()} ${errorBody ?: ""}".trim())
                }
                val sessionId = response.body()?.publicSessionId
                if (sessionId.isNullOrBlank()) {
                    throw Exception("Session creation returned empty ID")
                }
                sessionId
            }
                .onSuccess { sessionId ->
                    // Save ID session into state
                    state = state.copy(currentSessionId = sessionId, error = null)
                    // Have ID , start stream letter of AI
                    streamResponse(initialPrompt, isCampaign = true)
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
        viewModelScope.launch(ioDispatcher) {
            runCatching {
                val response = repository.createSession()
                if (!response.isSuccessful) {
                    val errorBody = response.errorBody()?.string()?.take(200)
                    throw Exception("Failed to create session: ${response.code()} ${errorBody ?: ""}".trim())
                }
                val sessionId = response.body()?.publicSessionId
                if (sessionId.isNullOrBlank()) {
                    throw Exception("Session creation returned empty ID")
                }
                sessionId
            }
                .onSuccess { sessionId ->
                    state = state.copy(currentSessionId = sessionId, error = null)
                    streamResponse(userText, isCampaign = false)
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

        viewModelScope.launch(ioDispatcher){

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
                    typeChunk(aiMessageId, formattedChunk)
                }
            val finalMessage = state.messages.map { msg ->
                if (msg.id == aiMessageId) msg.copy(isStreaming = false)
                else msg
            }
            state = state.copy(messages = finalMessage)
        }
    }
    private suspend fun appendAiChunk(aiMessageId: String, chunk: String) {
        withContext(Dispatchers.Main) {
            val updatedMessages = state.messages.map { msg ->
                if (msg.id == aiMessageId){
                    msg.copy(text = msg.text + chunk)
                }
                else msg
            }
            state = state.copy(messages = updatedMessages)
        }
    }

    private suspend fun typeChunk(aiMessageId: String, chunk: String) {
        if (chunk.isBlank()) {
            appendAiChunk(aiMessageId, chunk)
            return
        }

        val tokens = chunk.split(Regex("(?<=\\s)"))
        for (token in tokens) {
            appendAiChunk(aiMessageId, token)
            delay(28)
        }
    }

    fun clearError() {
        state = state.copy(error = null)
    }

    fun selectSession(sessionId: String) {
        if (sessionId.isBlank()) return
        state = state.copy(currentSessionId = sessionId, messages = emptyList(), error = null)
    }
}