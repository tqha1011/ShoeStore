package com.example.shoestoreapp.features.admin.product.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestoreapp.core.utils.uriToTempFile
import com.example.shoestoreapp.features.admin.product.EditProductUiState
import com.example.shoestoreapp.features.admin.product.ProductVariant
import com.example.shoestoreapp.features.admin.product.data.repositories.AdminProductRepository
import com.example.shoestoreapp.features.admin.product.data.repositories.AdminProductRepositoryImpl
import com.example.shoestoreapp.features.admin.product.data.repositories.ImageRepository
import com.example.shoestoreapp.features.admin.product.data.repositories.ImageRepositoryException
import com.example.shoestoreapp.features.admin.product.data.repositories.ImageRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminEditProductViewModel(
    private val repository: AdminProductRepository = AdminProductRepositoryImpl(),
    private val imageRepository: ImageRepository = ImageRepositoryImpl()
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        EditProductUiState(
            productName = "",
            brand = "",
            category = "",
            imageUrl = null,
            variants = emptyList()
        )
    )
    val uiState: StateFlow<EditProductUiState> = _uiState.asStateFlow()

    private val _localImageUri = MutableStateFlow<Uri?>(null)
    val localImageUri: StateFlow<Uri?> = _localImageUri.asStateFlow()

    private val _uploadedImageUrl = MutableStateFlow<String?>(null)
    val uploadedImageUrl: StateFlow<String?> = _uploadedImageUrl.asStateFlow()

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

                _uiState.value = _uiState.value.copy(
                    productName = dto.productName,
                    brand = dto.brand,
                    category = dto.categoryName,
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
    }

    fun onBrandChange(value: String) {
    }

    fun onCategoryChange(value: String) {
    }

    fun onSaveClick() {
    }

    fun onDeleteClick() {
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

    private fun formatSize(size: Double): String {
        val text = if (size % 1.0 == 0.0) size.toInt().toString() else size.toString()
        return "US $text"
    }
}