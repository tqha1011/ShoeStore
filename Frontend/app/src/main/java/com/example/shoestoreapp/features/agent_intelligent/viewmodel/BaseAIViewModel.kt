package com.example.shoestoreapp.features.agent_intelligent.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.PlatformTextInputSessionScope
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImagePainter
import com.example.shoestoreapp.features.agent_intelligent.data.remote.ChatSessionResponseDto
import com.example.shoestoreapp.features.agent_intelligent.data.repository.AiChatRepository
import com.example.shoestoreapp.features.agent_intelligent.viewmodel.ChatMessage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

data class AIChatState(
    val messages: List<ChatMessage> = emptyList(),
    val sessions: List<ChatSessionResponseDto> = emptyList(),
    val isLoadingSesions : Boolean = false, // Initial loading state ( Page 1 )
    val isMoreLoading: Boolean = false, // Pagination loading state ( Scroll to bottom )
    val currentPage : Int = 1,   // Tracks the current page for infinite scroll
    val isLastPage : Boolean = false, // Flag to stop fetching when no more data is available
    val currentSessionId : String? = null,
    val error : String? = null
)

abstract class BaseAIViewModel(
    protected val repository : AiChatRepository,
    protected val ioDispatcher : CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    var state by mutableStateOf(AIChatState())
        protected set

    // Subclasses MUST implement this to define which specific API endpoint to stream from
    protected abstract fun getStreamFlow (sessionId: String, prompt: String, isCampaign: Boolean) : Flow<String>

    open fun initialize(initialPrompt: String?) {}

    fun loadSessions(isNextPage : Boolean = false) {
        // Prevent concurrent API calls or fetching if we already reached the last page
       if ((isNextPage && state.isLastPage) || state.isLoadingSesions || state.isMoreLoading) return

        viewModelScope.launch (ioDispatcher){
            val pageLoad = if (isNextPage) state.currentPage + 1 else 1

            state = if (isNextPage) state.copy(isMoreLoading = true, error = null)
            else state.copy(isLoadingSesions = true, error = null)

            runCatching {
                val response = repository.getSessions(pageNumber = pageLoad, pageSize = 10)
                if (response.isSuccessful) response.body()
                else throw Exception("Failed to load history: ${response.code()}")
            }
                .onSuccess { data ->
                    val newItems = data?.items ?: emptyList()
                    val hasNext = data?.hasNext ?: false

                    state = state.copy(
                        sessions = if (isNextPage) state.sessions + newItems else newItems,
                        currentPage = pageLoad,
                        isLastPage = !hasNext,
                        isLoadingSesions = false,
                        isMoreLoading = false
                    )
                }
                .onFailure { exception ->
                    state = state.copy(
                        error = exception.message,
                        isLoadingSesions = false,
                        isMoreLoading = false
                    )
                }
        }
    }
    fun SendMessage(userText : String, isCampaign: Boolean = false) {
        if (userText.isBlank()) return
        val userMsg = ChatMessage(text = userText, isUser = true)
        state = state.copy(messages = state.messages + userMsg, error = null)

        val sessionId = state.currentSessionId
        if (sessionId == null) {
            startNewSessionAndSendMessage(userText,isCampaign)
        }
        else {
            streamResponse(userText, isCampaign)
        }
    }

    private fun startNewSessionAndSendMessage(userText: String, isCampaign: Boolean) {
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
                    streamResponse(userText, isCampaign)
                }
                .onFailure { exception ->
                    val errorMsg =
                        ChatMessage(text = "Network error : ${exception.message}", isUser = false)
                    state = state.copy(messages = state.messages + errorMsg, error = exception.message)
                }
        }
    }

    fun streamResponse(prompt : String, isCampaign : Boolean){
        val sessionId = state.currentSessionId ?: return
        // Create Bubble Chat for user messages

        // Create Bubble Chat for AI messages
        val aiMessageId = java.util.UUID.randomUUID().toString()

        // Create an empty placeholder message for the AI with the streaming flag turned on
        val aiMsgPlaceHolder = ChatMessage(id = aiMessageId, text = "", isUser = false, isStreaming = true)
        state = state.copy(messages = state.messages + aiMsgPlaceHolder, error = null)



        viewModelScope.launch(ioDispatcher){
            val streamFlow = getStreamFlow(sessionId, prompt, isCampaign)

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