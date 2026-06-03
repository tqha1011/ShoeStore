package com.example.shoestoreapp.features.admin.product.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestoreapp.core.utils.Resource
import com.example.shoestoreapp.core.utils.uriToTempFile
import com.example.shoestoreapp.features.admin.addproduct.data.repositories.MasterDataRepository
import com.example.shoestoreapp.features.admin.product.data.models.Category
import com.example.shoestoreapp.features.admin.product.data.models.EditProductUiState
import com.example.shoestoreapp.features.admin.product.data.models.ShoeColor
import com.example.shoestoreapp.features.admin.product.data.models.ShoeSize
import com.example.shoestoreapp.features.admin.product.data.models.VariantUiState
import com.example.shoestoreapp.features.admin.product.data.remote.ProductVariantResponseDto
import com.example.shoestoreapp.features.admin.product.data.repositories.AdminProductRepository
import com.example.shoestoreapp.features.admin.product.data.repositories.AdminProductRepositoryImpl
import com.example.shoestoreapp.features.admin.product.data.repositories.ImageRepository
import com.example.shoestoreapp.features.admin.product.data.repositories.ImageRepositoryException
import com.example.shoestoreapp.features.admin.product.data.repositories.ImageRepositoryImpl
import com.example.shoestoreapp.features.admin.product.data.remote.UpdateProductDto
import com.example.shoestoreapp.features.admin.product.data.repositories.CreateVariantParams
import com.example.shoestoreapp.features.admin.product.data.repositories.UpdateVariantParams
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class AdminEditProductUiEvent {
    data class ShowError(val message: String) : AdminEditProductUiEvent()
    object UpdateSuccess : AdminEditProductUiEvent()
    object DeleteSuccess : AdminEditProductUiEvent()
    object VariantCreateSuccess : AdminEditProductUiEvent()
    object VariantUpdateSuccess : AdminEditProductUiEvent()
    object VariantDeleteSuccess : AdminEditProductUiEvent()
}

class AdminEditProductViewModel(
    private val repository: AdminProductRepository = AdminProductRepositoryImpl(),
    private val imageRepository: ImageRepository = ImageRepositoryImpl(),
    private val masterDataRepository: MasterDataRepository = MasterDataRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        EditProductUiState(
            productName = "",
            brand = "",
            selectedCategory = null,
            imageUrl = null,
            variants = emptyList()
        )
    )
    val uiState: StateFlow<EditProductUiState> = _uiState.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _sizes = MutableStateFlow<List<ShoeSize>>(emptyList())
    val sizes: StateFlow<List<ShoeSize>> = _sizes.asStateFlow()

    private val _colors = MutableStateFlow<List<ShoeColor>>(emptyList())
    val colors: StateFlow<List<ShoeColor>> = _colors.asStateFlow()

    private val _isAddVariantSheetVisible = MutableStateFlow(false)
    val isAddVariantSheetVisible: StateFlow<Boolean> = _isAddVariantSheetVisible.asStateFlow()

    private val _variantDraft = MutableStateFlow(VariantUiState())
    val variantDraft: StateFlow<VariantUiState> = _variantDraft.asStateFlow()

    private val _localImageUri = MutableStateFlow<Uri?>(null)
    val localImageUri: StateFlow<Uri?> = _localImageUri.asStateFlow()

    private val _uploadedImageUrl = MutableStateFlow<String?>(null)
    val uploadedImageUrl: StateFlow<String?> = _uploadedImageUrl.asStateFlow()

    private val _uiEvent = Channel<AdminEditProductUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val _showDeleteConfirmation = MutableStateFlow(false)
    val showDeleteConfirmation: StateFlow<Boolean> = _showDeleteConfirmation.asStateFlow()

    private val _variantToDelete = MutableStateFlow<ProductVariantResponseDto?>(null)
    val variantToDelete: StateFlow<ProductVariantResponseDto?> = _variantToDelete.asStateFlow()

    init {
        fetchCategories()
        fetchMasterData()
    }

    fun hideBanner() {
        _uiState.update { it.copy(showBanner = false) }
    }

    fun loadProductDetails(productId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.getProductById(productId)
            result.onSuccess { dto ->
                val imageUrl = dto.variants.firstOrNull { !it.imageUrl.isNullOrBlank() }?.imageUrl
                val category = _categories.value.firstOrNull { it.id == dto.categoryId }
                    ?: Category(dto.categoryId, dto.categoryName)
                val activeVariants = dto.variants.filterNot { it.isDelete }

                _uiState.update {
                    it.copy(
                        productName = dto.productName,
                        brand = dto.brand,
                        selectedCategory = category,
                        imageUrl = imageUrl,
                        variants = activeVariants,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message
                    )
                }
            }
        }
    }

    fun onNameChange(value: String) {
        _uiState.update { it.copy(productName = value) }
    }

    fun onBrandChange(value: String) {
        _uiState.update { it.copy(brand = value) }
    }

    fun onCategoryChange(category: Category) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun onSaveClick(productId: String) {
        val current = _uiState.value
        if (current.productName.isBlank() || current.brand.isBlank()) {
            _uiState.update {
                it.copy(
                    bannerMessage = "Product name and brand are required.",
                    isBannerSuccess = false,
                    showBanner = true
                )
            }
            return
        }
        val selectedCategory = current.selectedCategory
        if (selectedCategory == null) {
            _uiState.update {
                it.copy(
                    bannerMessage = "Please select a category.",
                    isBannerSuccess = false,
                    showBanner = true
                )
            }
            return
        }

        val dto = UpdateProductDto(
            brand = current.brand,
            categoryId = selectedCategory.id,
            productName = current.productName
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val result = repository.updateProduct(productId, dto)
                result.onSuccess {
                    _uiState.update {
                        it.copy(
                            bannerMessage = "Update product success",
                            isBannerSuccess = true,
                            showBanner = true
                        )
                    }
                    _uiEvent.send(AdminEditProductUiEvent.UpdateSuccess)
                }.onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            bannerMessage = throwable.message ?: "Update failed.",
                            isBannerSuccess = false,
                            showBanner = true
                        )
                    }
                }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onDeleteClicked() {
        _showDeleteConfirmation.value = true
    }

    fun onDismissDeleteDialog() {
        _showDeleteConfirmation.value = false
    }

    fun confirmDelete(productId: String) {
        _showDeleteConfirmation.value = false

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val result = repository.deleteProduct(productId)
                result.onSuccess {
                    _uiState.update {
                        it.copy(
                            bannerMessage = "Delete product success",
                            isBannerSuccess = true,
                            showBanner = true
                        )
                    }
                    _uiEvent.send(AdminEditProductUiEvent.DeleteSuccess)
                }.onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            bannerMessage = throwable.message ?: "Delete failed.",
                            isBannerSuccess = false,
                            showBanner = true
                        )
                    }
                }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onEditVariantClick(variant: ProductVariantResponseDto) {
        val size = _sizes.value.firstOrNull { it.id == variant.sizeId }
        val color = _colors.value.firstOrNull { it.id == variant.colorId }

        _variantDraft.value = _variantDraft.value.copy(
            variantId = variant.publicId,
            existingImageUrl = variant.imageUrl,
            imageUri = null,
            imageUrl = null,
            selectedSize = size,
            selectedColor = color,
            price = variant.price.toString(),
            stock = variant.stock.toString()
        )
        _isAddVariantSheetVisible.value = true
    }

    fun onDeleteVariantClick(variant: ProductVariantResponseDto) {
        _variantToDelete.value = variant
    }

    fun onDismissDeleteVariantDialog() {
        _variantToDelete.value = null
    }

    fun confirmDeleteVariant(productId: String) {
        val variantId = _variantToDelete.value?.publicId ?: return
        _variantToDelete.value = null

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val result = repository.deleteVariant(productId, variantId)
                result.onSuccess {
                    loadProductDetails(productId)
                    _uiState.update {
                        it.copy(
                            bannerMessage = "Variant deleted",
                            isBannerSuccess = true,
                            showBanner = true
                        )
                    }
                    _uiEvent.send(AdminEditProductUiEvent.VariantDeleteSuccess)
                }.onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            bannerMessage = throwable.message ?: "Delete variant failed.",
                            isBannerSuccess = false,
                            showBanner = true
                        )
                    }
                }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onAddVariantClick() {
        _variantDraft.value = VariantUiState()
        _isAddVariantSheetVisible.value = true
    }

    fun onDismissVariantSheet() {
        _isAddVariantSheetVisible.value = false
    }

    fun updateVariantDraft(
        size: ShoeSize? = null,
        color: ShoeColor? = null,
        price: String? = null,
        stock: String? = null,
        uri: Uri? = null
    ) {
        val current = _variantDraft.value
        _variantDraft.value = current.copy(
            selectedSize = size ?: current.selectedSize,
            selectedColor = color ?: current.selectedColor,
            price = price ?: current.price,
            stock = stock ?: current.stock,
            imageUri = uri ?: current.imageUri
        )
    }

    fun uploadSelectedImage(context: Context, uri: Uri) {
        _localImageUri.value = uri

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = try {
                val file = context.applicationContext.uriToTempFile(uri)
                imageRepository.uploadImage(file)
            } catch (e: Exception) {
                Result.failure(ImageRepositoryException.Unknown(e.message ?: "Unable to upload image right now."))
            }

            result.onSuccess { response ->
                _uploadedImageUrl.value = response.imageUrl
                _uiState.update { it.copy(isLoading = false) }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        bannerMessage = throwable.message ?: "Unable to upload image.",
                        isBannerSuccess = false,
                        showBanner = true
                    )
                }
            }
        }
    }

    fun uploadVariantImage(context: Context, uri: Uri) {
        _variantDraft.value = _variantDraft.value.copy(imageUri = uri, isUploadingImage = true)

        viewModelScope.launch {
            val result = try {
                val file = context.applicationContext.uriToTempFile(uri)
                imageRepository.uploadImage(file)
            } catch (e: Exception) {
                Result.failure(ImageRepositoryException.Unknown(e.message ?: "Unable to upload image right now."))
            }

            result.onSuccess { response ->
                _variantDraft.value = _variantDraft.value.copy(
                    imageUrl = response.imageUrl,
                    isUploadingImage = false
                )
            }.onFailure { throwable ->
                _variantDraft.value = _variantDraft.value.copy(isUploadingImage = false)
                _uiState.update {
                    it.copy(
                        bannerMessage = throwable.message ?: "Unable to upload image right now.",
                        isBannerSuccess = false,
                        showBanner = true
                    )
                }
            }
        }
    }

    fun onSaveVariant(context: Context, productId: String) {
        val draft = _variantDraft.value
        val size = draft.selectedSize
        val color = draft.selectedColor
        if (size == null || color == null) {
            _uiState.update {
                it.copy(
                    bannerMessage = "Please select size and color.",
                    isBannerSuccess = false,
                    showBanner = true
                )
            }
            return
        }

        val priceValue = draft.price.toDoubleOrNull()
        if (priceValue == null || priceValue <= 0.0) {
            _uiState.update {
                it.copy(
                    bannerMessage = "Please enter a valid price.",
                    isBannerSuccess = false,
                    showBanner = true
                )
            }
            return
        }

        val stockValue = draft.stock.toIntOrNull()
        if (stockValue == null || stockValue < 0) {
            _uiState.update {
                it.copy(
                    bannerMessage = "Please enter a valid stock.",
                    isBannerSuccess = false,
                    showBanner = true
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val imageFile = draft.imageUri?.let { uri ->
                    try {
                        context.applicationContext.uriToTempFile(uri)
                    } catch (e: Exception) {
                        _uiState.update {
                            it.copy(
                                bannerMessage = e.message ?: "Unable to prepare image right now.",
                                isBannerSuccess = false,
                                showBanner = true
                            )
                        }
                        return@launch
                    }
                }

                val result = if (draft.variantId == null) {
                    repository.createVariant(
                        productId = productId,
                        params = CreateVariantParams(
                            sizeId = size.id,
                            colorId = color.id,
                            stock = stockValue,
                            price = priceValue,
                            isSelling = true,
                            imageFile = imageFile
                        )
                    ).map { Unit }
                } else {
                    repository.updateVariant(
                        productId = productId,
                        variantId = draft.variantId,
                        params = UpdateVariantParams(
                            sizeId = size.id,
                            colorId = color.id,
                            stock = stockValue,
                            price = priceValue,
                            isSelling = true,
                            imageUrl = draft.existingImageUrl ?: "",
                            imageFile = imageFile
                        )
                    )
                }

                result.onSuccess {
                    _isAddVariantSheetVisible.value = false
                    _variantDraft.value = VariantUiState()
                    loadProductDetails(productId)
                    val message = if (draft.variantId == null) "Create variant success" else "Update variant success"
                    _uiState.update {
                        it.copy(
                            bannerMessage = message,
                            isBannerSuccess = true,
                            showBanner = true
                        )
                    }
                    if (draft.variantId == null) {
                        _uiEvent.send(AdminEditProductUiEvent.VariantCreateSuccess)
                    } else {
                        _uiEvent.send(AdminEditProductUiEvent.VariantUpdateSuccess)
                    }
                }.onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            bannerMessage = throwable.message ?: if (draft.variantId == null) {
                                "Create variant failed."
                            } else {
                                "Update variant failed."
                            },
                            isBannerSuccess = false,
                            showBanner = true
                        )
                    }
                }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun fetchCategories() {
        viewModelScope.launch {
            masterDataRepository.getCategories().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _categories.value = resource.data.map { dto ->
                            Category(
                                id = dto.id.toIntOrNull() ?: 0,
                                name = dto.categoryName
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                bannerMessage = resource.message,
                                isBannerSuccess = false,
                                showBanner = true
                            )
                        }
                    }
                    Resource.Loading -> Unit
                }
            }
        }
    }

    private fun fetchMasterData() {
        viewModelScope.launch {
            masterDataRepository.getSizes().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _sizes.value = resource.data.map { dto ->
                            ShoeSize(
                                id = dto.id.toIntOrNull() ?: 0,
                                value = dto.sizeValue
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                bannerMessage = resource.message,
                                isBannerSuccess = false,
                                showBanner = true
                            )
                        }
                    }
                    Resource.Loading -> Unit
                }
            }
        }

        viewModelScope.launch {
            masterDataRepository.getColors().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _colors.value = resource.data.map { dto ->
                            ShoeColor(
                                id = dto.id.toIntOrNull() ?: 0,
                                name = dto.colorName,
                                hexCode = ""
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                bannerMessage = resource.message,
                                isBannerSuccess = false,
                                showBanner = true
                            )
                        }
                    }
                    Resource.Loading -> Unit
                }
            }
        }
    }
}