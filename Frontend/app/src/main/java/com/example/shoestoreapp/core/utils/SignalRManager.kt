package com.example.shoestoreapp.core.utils

import android.util.Log
import com.example.shoestoreapp.features.user.ai_assistant.data.remote.AddVariantResultDto
import com.example.shoestoreapp.features.user.ai_assistant.data.remote.SearchProductResultDto
import com.google.gson.Gson
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.firstOrNull

class SignalRManager(private val tokenManager: TokenManager) {
    // SignalR Connection
    private var hubConnection: HubConnection? = null
    // AddVariantResult
    private val _variantDraftFlow = MutableSharedFlow<AddVariantResultDto?>(replay = 0)
    val variantDraftFlow = _variantDraftFlow.asSharedFlow()

    // SearchProductResult
    private val _searchResultFlow = MutableSharedFlow<SearchProductResultDto?>(replay = 0)
    val searchResultFlow = _searchResultFlow.asSharedFlow()
    suspend fun startConnection() {
        if (hubConnection?.connectionState == HubConnectionState.CONNECTED) return

        try {
            val jwtToken = tokenManager.getToken.firstOrNull() ?: ""
            val serverUrl = "https://overhaul-pampered-landslide.ngrok-free.dev/hubs/agent/notify"

            hubConnection = HubConnectionBuilder.create(serverUrl)
                .withAccessTokenProvider(Single.defer { Single.just(jwtToken) })
                .withHeader("ngrok-skip-browser-warning", "true") // Avoid Preventing Ngrok Screen
                .build()

            registerAiAgentListeners()

            hubConnection?.start()?.blockingAwait()
            Log.d("SIGNAL_R", "Open SignalR successfully")
        } catch (e: Exception) {
            Log.e("SIGNAL_R", "Error SignalR: ${e.message}")
        }
    }

    private fun registerAiAgentListeners() {
        hubConnection?.on("NotifyAddVariantResponse", { rawJson ->
            try {
                val result = Gson().fromJson(rawJson, AddVariantResultDto::class.java)
                _variantDraftFlow.tryEmit(result)
            } catch (e: Exception) {
                Log.e("SIGNAL_R", "Lỗi parse JSON: ${e.message}")
            }
        }, String::class.java)

        hubConnection?.on("NotifySearchResultAsync", { rawJson ->
            try {
                val result = Gson().fromJson(rawJson, SearchProductResultDto::class.java)
                _searchResultFlow.tryEmit(result)
            } catch (e: Exception) {
                Log.e("SIGNAL_R", "Lỗi parse Search: ${e.message}")
            }
        }, String::class.java)
    }

    fun stopConnection() {
        hubConnection?.stop()
    }
}