package com.example.shoestoreapp.features.admin.addproduct.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestoreapp.features.admin.addproduct.data.repositories.AdminProductRepository
import com.example.shoestoreapp.features.admin.addproduct.data.repositories.AdminProductRepositoryImpl
import com.example.shoestoreapp.features.admin.product.data.remote.CreateProductDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AddProductUiState {
    data object Idle : AddProductUiState
    data object Loading : AddProductUiState
    data object Success : AddProductUiState
    data class Error(val message: String) : AddProductUiState
}

class AdminAddProductViewModel(
    private val repository: AdminProductRepository = AdminProductRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddProductUiState>(AddProductUiState.Idle)
    val uiState: StateFlow<AddProductUiState> = _uiState.asStateFlow()

    fun saveProduct(productName: String, categoryId: Int) {
        _uiState.value = AddProductUiState.Loading

        if (productName.isBlank()) {
            _uiState.value = AddProductUiState.Error("Product name cannot be empty.")
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
                    _uiState.value = AddProductUiState.Success
                }
                .onFailure { exception ->
                    _uiState.value = AddProductUiState.Error(
                        exception.message ?: "Unable to create product."
                    )
                }
        }
    }

    fun resetState() {
        _uiState.value = AddProductUiState.Idle
    }
}
