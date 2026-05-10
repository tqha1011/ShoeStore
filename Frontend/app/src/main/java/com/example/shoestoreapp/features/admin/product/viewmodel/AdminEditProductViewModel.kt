package com.example.shoestoreapp.features.admin.product.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestoreapp.core.utils.Resource
import com.example.shoestoreapp.core.utils.uriToTempFile
import com.example.shoestoreapp.features.admin.addproduct.data.repositories.MasterDataRepository
import com.example.shoestoreapp.features.admin.product.Category
import com.example.shoestoreapp.features.admin.product.EditProductUiState
import com.example.shoestoreapp.features.admin.product.ProductVariant
import com.example.shoestoreapp.features.admin.product.data.repositories.AdminProductRepository
import com.example.shoestoreapp.features.admin.product.data.repositories.AdminProductRepositoryImpl
import com.example.shoestoreapp.features.admin.product.data.repositories.ImageRepository
import com.example.shoestoreapp.features.admin.product.data.repositories.ImageRepositoryException
import com.example.shoestoreapp.features.admin.product.data.repositories.ImageRepositoryImpl
import com.example.shoestoreapp.features.admin.product.data.remote.UpdateProductDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

sealed class AdminEditProductUiEvent {
    data class ShowError(val message: String) : AdminEditProductUiEvent()
    object UpdateSuccess : AdminEditProductUiEvent()
    object DeleteSuccess : AdminEditProductUiEvent()
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

    private val _localImageUri = MutableStateFlow<Uri?>(null)
    val localImageUri: StateFlow<Uri?> = _localImageUri.asStateFlow()

    private val _uploadedImageUrl = MutableStateFlow<String?>(null)
    val uploadedImageUrl: StateFlow<String?> = _uploadedImageUrl.asStateFlow()

    private val _uiEvent = Channel<AdminEditProductUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val _showDeleteConfirmation = MutableStateFlow(false)
    val showDeleteConfirmation: StateFlow<Boolean> = _showDeleteConfirmation.asStateFlow()

    init {
        fetchCategories()
    }

    fun loadProductDetails(productId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = repository.getProductById(productId)
            result.onSuccess { dto ->
                val variants = dto.variants.map { variant ->
                    ProductVariant(
                        size = formatSize(variant.size),
                        color = variant.colorName ?: "Unknown",
                        stock = variant.stock
                    )
                }
                val imageUrl = dto.variants.firstOrNull { !it.imageUrl.isNullOrBlank() }?.imageUrl
                val category = _categories.value.firstOrNull { it.id == dto.categoryId }
                    ?: Category(dto.categoryId, dto.categoryName)

                _uiState.value = _uiState.value.copy(
                    productName = dto.productName,
                    brand = dto.brand,
                    selectedCategory = category,
                    imageUrl = imageUrl,
                    variants = variants,
                    isLoading = false,
                    errorMessage = null
                )
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = throwable.message
                )
            }
        }
    }

    fun onNameChange(value: String) {
        _uiState.value = _uiState.value.copy(productName = value)
    }

    fun onBrandChange(value: String) {
        _uiState.value = _uiState.value.copy(brand = value)
    }

    fun onCategoryChange(category: Category) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun onSaveClick(productId: String) {
        val current = _uiState.value
        if (current.productName.isBlank() || current.brand.isBlank()) {
            _uiEvent.trySend(AdminEditProductUiEvent.ShowError("Product name and brand are required."))
            return
        }
        val selectedCategory = current.selectedCategory
        if (selectedCategory == null) {
            _uiEvent.trySend(AdminEditProductUiEvent.ShowError("Please select a category."))
            return
        }

        val dto = UpdateProductDto(
            brand = current.brand,
            categoryId = selectedCategory.id,
            productName = current.productName
        )

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val result = repository.updateProduct(productId, dto)
                result.onSuccess {
                    _uiEvent.send(AdminEditProductUiEvent.UpdateSuccess)
                }.onFailure { throwable ->
                    _uiEvent.send(
                        AdminEditProductUiEvent.ShowError(
                            throwable.message ?: "Update failed."
                        )
                    )
                }
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
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
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val result = repository.deleteProduct(productId)
                result.onSuccess {
                    _uiEvent.send(AdminEditProductUiEvent.DeleteSuccess)
                }.onFailure { throwable ->
                    _uiEvent.send(
                        AdminEditProductUiEvent.ShowError(
                            throwable.message ?: "Delete failed."
                        )
                    )
                }
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun onEditVariantClick(variant: ProductVariant) {
    }

    fun onDeleteVariantClick(variant: ProductVariant) {
    }

    fun onAddVariantClick() {
    }

    fun uploadSelectedImage(context: Context, uri: Uri) {
        _localImageUri.value = uri

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = try {
                val file = context.applicationContext.uriToTempFile(uri)
                imageRepository.uploadImage(file)
            } catch (e: Exception) {
                Result.failure(ImageRepositoryException.Unknown(e.message ?: "Unable to upload image right now."))
            }

            result.onSuccess { url ->
                _uploadedImageUrl.value = url
                _uiState.value = _uiState.value.copy(isLoading = false)
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = throwable.message
                )
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
                        _uiEvent.trySend(AdminEditProductUiEvent.ShowError(resource.message))
                    }
                    Resource.Loading -> Unit
                }
            }
        }
    }

    private fun formatSize(size: Double): String {
        val text = if (size % 1.0 == 0.0) size.toInt().toString() else size.toString()
        return "US $text"
    }
}
