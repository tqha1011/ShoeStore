package com.example.shoestoreapp.features.admin.addproduct.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestoreapp.features.admin.addproduct.data.repositories.AdminProductRepository
import com.example.shoestoreapp.features.admin.addproduct.data.repositories.AdminProductRepositoryImpl
import com.example.shoestoreapp.features.admin.product.data.remote.CreateProductDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddProductUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val bannerMessage: String = "",
    val isBannerSuccess: Boolean = true,
    val showBanner: Boolean = false
)

class AdminAddProductViewModel(
    private val repository: AdminProductRepository = AdminProductRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddProductUiState())
    val uiState: StateFlow<AddProductUiState> = _uiState.asStateFlow()

    fun saveProduct(productName: String, categoryId: Int) {
        _uiState.update { it.copy(isLoading = true, showBanner = false) }

        if (productName.isBlank()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    bannerMessage = "Product name cannot be empty.",
                    isBannerSuccess = false,
                    showBanner = true
                )
            }
            return
        }

        val dto = CreateProductDto(
            productName = productName,
            categoryId = categoryId,
            brand = "Nike"
        )

        viewModelScope.launch {
            repository.createProduct(dto)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = true,
                            bannerMessage = "Product created successfully.",
                            isBannerSuccess = true,
                            showBanner = true
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            bannerMessage = exception.message ?: "Unable to create product.",
                            isBannerSuccess = false,
                            showBanner = true
                        )
                    }
                }
        }
    }

    fun resetState() {
        _uiState.update { it.copy(showBanner = false) }
    }
}