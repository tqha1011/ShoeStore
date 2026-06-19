package com.example.shoestoreapp.core.utils

import android.util.Log
import com.example.shoestoreapp.features.agent_intelligent.data.remote.AddVariantResultDto
import com.example.shoestoreapp.features.agent_intelligent.data.remote.SearchProductResultDto
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
    private val connectionLock = Any()
    private var isStartingConnection = false
    // AddVariantResult
    private val _variantDraftFlow = MutableSharedFlow<AddVariantResultDto?>(replay = 1)
    val variantDraftFlow = _variantDraftFlow.asSharedFlow()

    // SearchProductResult
    private val _searchResultFlow = MutableSharedFlow<SearchProductResultDto?>(replay = 1)
    val searchResultFlow = _searchResultFlow.asSharedFlow()
    suspend fun startConnection() {
        synchronized(connectionLock) {
            if (hubConnection?.connectionState == HubConnectionState.CONNECTED || isStartingConnection) return
            isStartingConnection = true
        }

        try {
            val jwtToken = tokenManager.getToken.firstOrNull() ?: ""
            val serverUrl = "${Constants.BASE_URL.trimEnd('/')}/hubs/agent/notify"

            hubConnection = HubConnectionBuilder.create(serverUrl)
                .withAccessTokenProvider(Single.defer { Single.just(jwtToken) })
                .withHeader("ngrok-skip-browser-warning", "true") // Avoid Preventing Ngrok Screen
                .build()

            registerAiAgentListeners()

            hubConnection?.start()?.blockingAwait()
            Log.d("SIGNAL_R", "Open SignalR successfully")
        } catch (e: Exception) {
            Log.e("SIGNAL_R", "Error SignalR: ${e.message}")
        } finally {
            synchronized(connectionLock) {
                isStartingConnection = false
            }
        }
    }

    private fun registerAiAgentListeners() {
        // 1. Lắng nghe AddVariant (Ném thẳng class AddVariantResultDto vào)
        hubConnection?.on("NotifyAddVariantResponse", { result ->
            Log.d("DEBUG_FLOW", "Trạm 1 [SignalR]: Parse tự động thành công! Status AddVariant -> ${result.status}")
            // result lúc này đã là object AddVariantResultDto rồi, không cần Gson().fromJson nữa
            _variantDraftFlow.tryEmit(result)
        }, AddVariantResultDto::class.java)

        // 2. Lắng nghe Search (Ném thẳng class SearchProductResultDto vào)
        hubConnection?.on("NotifySearchResultAsync", { result ->
            Log.d("DEBUG_FLOW", "Trạm 1 [SignalR]: Parse tự động thành công! Status Search -> ${result.status}")
            // result lúc này đã là object SearchProductResultDto
            _searchResultFlow.tryEmit(result)
        }, SearchProductResultDto::class.java)
    }

    fun stopConnection() {
        hubConnection?.stop()
    }
}
