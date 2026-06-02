    package com.example.shoestoreapp.features.user.ai_assistant.viewmodel

    import androidx.lifecycle.viewModelScope
    import com.example.shoestoreapp.core.utils.SignalRManager
    import com.example.shoestoreapp.features.user.ai_assistant.data.remote.AddVariantResultDto
    import com.example.shoestoreapp.features.agent_intelligent.data.repository.AiChatRepository
    import com.example.shoestoreapp.features.agent_intelligent.viewmodel.BaseAIViewModel
    import com.example.shoestoreapp.features.user.ai_assistant.data.remote.SearchProductResultDto
    import kotlinx.coroutines.CoroutineDispatcher
    import kotlinx.coroutines.Dispatchers
    import kotlinx.coroutines.flow.Flow
    import kotlinx.coroutines.flow.MutableStateFlow
    import kotlinx.coroutines.flow.asStateFlow
    import kotlinx.coroutines.flow.collectLatest
    import kotlinx.coroutines.launch


    class AiProductViewmodel(
        repository: AiChatRepository,
        signalRManager: SignalRManager,
        ioDispatcher: CoroutineDispatcher = Dispatchers.IO
    ): BaseAIViewModel(repository, signalRManager, ioDispatcher) {
        private val _searchResultState = MutableStateFlow<SearchProductResultDto?>(null)
        val searchResultState = _searchResultState.asStateFlow()

        private val _variantDraftState = MutableStateFlow<AddVariantResultDto?>(null)
        val variantDraftState = _variantDraftState.asStateFlow()

        init {
            viewModelScope.launch(ioDispatcher) {
                signalRManager.searchResultFlow.collectLatest { result ->
                    _searchResultState.value = result
                }
            }
            viewModelScope.launch(ioDispatcher) {
                signalRManager.variantDraftFlow.collectLatest { draftResult ->
                    _variantDraftState.value = draftResult
                }
            }
        }
        override fun getStreamFlow(sessionId: String, prompt: String, isCampaign: Boolean): Flow<String> {
            return repository.streamChatProducts(sessionId, prompt)
        }
        fun clearSearchResult() {
            _searchResultState.value = null
        }

        fun clearVariantDraft() {
            _variantDraftState.value = null
        }

        fun submitVariantDraft(actionText: String) {
            //Send request to bot
            SendMessage(userText = actionText, isCampaign = false)
            clearVariantDraft()
        }

    }