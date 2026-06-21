package com.example.shoestoreapp.features.agent_intelligent.product_assistant.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.shoestoreapp.core.utils.Resource
import com.example.shoestoreapp.core.utils.SignalRManager
import com.example.shoestoreapp.features.admin.addproduct.data.remote.master_data.CategoryDto
import com.example.shoestoreapp.features.admin.addproduct.data.remote.master_data.ColorDto
import com.example.shoestoreapp.features.admin.addproduct.data.remote.master_data.SizeDto
import com.example.shoestoreapp.features.admin.addproduct.data.repositories.MasterDataRepository
import com.example.shoestoreapp.features.admin.product.data.repositories.AdminProductRepository
import com.example.shoestoreapp.features.admin.product.data.repositories.AdminProductRepositoryImpl
import com.example.shoestoreapp.features.admin.product.data.repositories.CreateVariantParams
import com.example.shoestoreapp.features.agent_intelligent.data.remote.AddVariantResultDto
import com.example.shoestoreapp.features.agent_intelligent.data.remote.ProductSummaryForLlm
import com.example.shoestoreapp.features.agent_intelligent.data.remote.SearchProductResultDto
import com.example.shoestoreapp.features.agent_intelligent.data.repository.AiChatRepository
import com.example.shoestoreapp.features.agent_intelligent.viewmodel.BaseAIViewModel
import com.example.shoestoreapp.features.agent_intelligent.viewmodel.ChatMessage
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
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val enableAdminActions: Boolean = false,
    private val adminProductRepository: AdminProductRepository = AdminProductRepositoryImpl(),
    private val masterDataRepository: MasterDataRepository = MasterDataRepository()
) : BaseAIViewModel(repository, signalRManager, ioDispatcher) {
    private val _searchResultState = MutableStateFlow<SearchProductResultDto?>(null)
    val searchResultState = _searchResultState.asStateFlow()

    private val _selectedProductState = MutableStateFlow<ProductSummaryForLlm?>(null)
    val selectedProductState = _selectedProductState.asStateFlow()

    private val _variantDraftState = MutableStateFlow<AddVariantResultDto?>(null)
    val variantDraftState = _variantDraftState.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage = _successMessage.asStateFlow()

    private var allowAdminSignals: Boolean = false
    private val adminActionRegex = Regex(
        "(thêm|add|variant|màu|size|giày|product\\s*id|select|choose|chọn|xác nhận|confirm|đồng ý|tạo biến thể|create)",
        RegexOption.IGNORE_CASE
    )

    private val _sizesList = MutableStateFlow<List<SizeDto>>(emptyList())
    private val _colorsList = MutableStateFlow<List<ColorDto>>(emptyList())
    private val _categoriesList = MutableStateFlow<List<CategoryDto>>(emptyList())

    val sizesList = _sizesList.asStateFlow()
    val colorsList = _colorsList.asStateFlow()
    val categoriesList = _categoriesList.asStateFlow()

    init {
        viewModelScope.launch(ioDispatcher) {
            signalRManager.searchResultFlow.collectLatest { result ->
                if (shouldAcceptAdminSignal(result?.status)) {
                    allowAdminSignals = true
                    _searchResultState.value = result
                }
            }
        }
        viewModelScope.launch(ioDispatcher) {
            signalRManager.variantDraftFlow.collectLatest { draftResult ->
                if (shouldAcceptAdminSignal(draftResult?.status)) {
                    allowAdminSignals = true
                    _variantDraftState.value = draftResult
                }
            }
        }
        viewModelScope.launch(ioDispatcher) {
            masterDataRepository.getSizes().collect { res ->
                if (res is Resource.Success) _sizesList.value = res.data
            }
        }
        viewModelScope.launch(ioDispatcher) {
            masterDataRepository.getColors().collect { res ->
                if (res is Resource.Success) _colorsList.value = res.data
            }
        }
        viewModelScope.launch(ioDispatcher) {
            masterDataRepository.getCategories().collect { res ->
                if (res is Resource.Success) _categoriesList.value = res.data
            }
        }
    }

    override fun getStreamFlow(sessionId: String, prompt: String, isCampaign: Boolean): Flow<String> {
        return if (enableAdminActions) {
            repository.streamChatProductsAdmin(sessionId, prompt)
        } else {
            repository.streamChatProductsUser(sessionId, prompt)
        }
    }

    override fun onUserMessageSent(userText: String, isCampaign: Boolean) {
        if (!enableAdminActions) return
        allowAdminSignals = adminActionRegex.containsMatchIn(userText)
    }

    private fun shouldAcceptAdminSignal(status: String?): Boolean {
        if (!enableAdminActions) return false
        if (allowAdminSignals) return true
        val normalized = status?.lowercase() ?: return false
        return normalized.contains("success") ||
            normalized.contains("draft") ||
            normalized.contains("colornotfound") ||
            normalized.contains("search")
    }

    fun clearSearchResult() {
        _searchResultState.value = null
        if (_variantDraftState.value == null && _selectedProductState.value == null) {
            allowAdminSignals = false
        }
    }

    fun clearVariantDraft() {
        _variantDraftState.value = null
        if (_searchResultState.value == null && _selectedProductState.value == null) {
            allowAdminSignals = false
        }
    }

    fun clearSelectedProduct() {
        _selectedProductState.value = null
        if (_searchResultState.value == null && _variantDraftState.value == null) {
            allowAdminSignals = false
        }
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    fun submitVariantDraft(actionText: String) {
        SendMessage(userText = actionText, isCampaign = false)
        clearVariantDraft()
    }

    fun selectSearchProduct(product: ProductSummaryForLlm) {
        val productName = product.productName.orEmpty()
        val productId = product.publicId.orEmpty()
        if (productName.isBlank() && productId.isBlank()) return

        allowAdminSignals = true
        if (productId.isNotBlank()) {
            _selectedProductState.value = product
        }
        clearSearchResult()
    }

    fun forceAdminAction() {
        if (enableAdminActions) {
            allowAdminSignals = true
        }
    }

    fun addVariantViaApi(
        productId: String,
        sizeId: Int?,
        colorId: Int?,
        sizeValue: String,
        colorName: String,
        stockText: String,
        priceText: String
    ) {
        if (!enableAdminActions) return
        if (productId.isBlank()) {
            state = state.copy(error = "Missing product ID")
            return
        }
        val resolvedSizeId = sizeId ?: run {
            _sizesList.value.firstOrNull { it.sizeValue.equals(sizeValue, ignoreCase = true) }
                ?.id
                ?.toIntOrNull()
        }
        val resolvedSizeValue = sizeValue.toIntOrNull() ?: run {
            _sizesList.value.firstOrNull { it.id == resolvedSizeId?.toString() }
                ?.sizeValue
                ?.toIntOrNull()
        }
        val resolvedColorId = colorId ?: run {
            _colorsList.value.firstOrNull { it.colorName.equals(colorName, ignoreCase = true) }
                ?.id
                ?.toIntOrNull()
        }
        val resolvedColorName = if (colorName.isNotBlank()) colorName else {
            _colorsList.value.firstOrNull { it.id == resolvedColorId?.toString() }?.colorName.orEmpty()
        }
        val resolvedStock = stockText.toIntOrNull()
        val resolvedPrice = priceText.toDoubleOrNull()

        if (resolvedSizeId == null || resolvedSizeValue == null || resolvedColorId == null || resolvedStock == null || resolvedPrice == null) {
            state = state.copy(error = "Please fill size/color/stock/price correctly")
            return
        }
        if (resolvedColorName.isBlank()) {
            state = state.copy(error = "Please select a color")
            return
        }

        val request = CreateVariantParams(
            sizeId = resolvedSizeId,
            colorId = resolvedColorId,
            stock = resolvedStock,
            price = resolvedPrice,
            isSelling = true,
            imageFile = null
        )

        viewModelScope.launch(ioDispatcher) {
            adminProductRepository.createVariant(productId, request)
                .onSuccess {
                    _variantDraftState.value = null
                    _selectedProductState.value = null
                    _successMessage.value = "Variant created successfully."
                    val successMessage = ChatMessage(
                        text = "Variant created successfully.",
                        isUser = false
                    )
                    state = state.copy(messages = state.messages + successMessage, error = null)
                }
                .onFailure { error ->
                    state = state.copy(error = error.message)
                }
        }
    }
}
