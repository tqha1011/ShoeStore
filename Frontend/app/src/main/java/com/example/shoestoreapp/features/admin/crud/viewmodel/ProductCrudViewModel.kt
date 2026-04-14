package com.example.shoestoreapp.features.admin.crud.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestoreapp.features.admin.crud.data.remote.ProductVariantDto
import com.example.shoestoreapp.features.admin.crud.data.repositories.ProductCrudRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


/**
 * ViewModel for Product CRUD operations
 * Manages form state, validation, and API calls
 */
class ProductCrudViewModel(
    private val repository: ProductCrudRepository
) : ViewModel() {
    private val _productName = MutableStateFlow("")
    val productName: StateFlow<String> = _productName.asStateFlow()

    private val _productType = MutableStateFlow("")
    val shoeType: StateFlow<String> = _productType.asStateFlow()

    private val _price = MutableStateFlow(0.0)
    val price:StateFlow<Double> = _price.asStateFlow()

    private val _productId = MutableStateFlow<String?>(null)
    val productId: StateFlow<String?> = _productId.asStateFlow()

    private val _stock = MutableStateFlow(0)
    val stock: StateFlow<Int> = _stock.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _imageUrl = MutableStateFlow<String?>(null)
    val imageUrl: StateFlow<String?> = _imageUrl.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun onProductNameChange(newName: String) {
        _productName.value = newName
    }

    fun onProductTypeChange(newType: String) {
        _productType.value = newType
    }

    fun onPriceChange(newPrice: Double) {
        _price.value = newPrice
    }

    fun onStockChange(newStock: Int) {
        _stock.value = newStock
    }

    fun onImageUrlChange(newUrl: String) {
        _imageUrl.value = newUrl
    }

    fun onProductIdChange(newId: String?) {
        _productId.value = newId
    }


    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun onSaveProduct() {
        viewModelScope.launch {
            _isLoading.value = true
            val productId = _productId.value
            val productName = _productName.value
            val brand = _productType.value 
            val variants = listOf(
                ProductVariantDto(
                    stock = _stock.value,
                    stockStatus = when {
                        _stock.value >= 10 -> "IN_STOCK"
                        _stock.value > 0 -> "LOW_STOCK"
                        else -> "OUT_OF_STOCK"
                    },
                    price = _price.value,
                    imageUrl = _imageUrl.value,
                )
            )

            val saveFlow = if (productId == null) {
                // TẠO MỚI
                repository.adminCreateProduct(
                    productName,
                    variants,
                    brand
                )
            } else {
                // CẬP NHẬT
                repository.adminUpdateProduct(productId, productName, variants)
            }

            // 4. "Hứng" kết quả từ Flow
            saveFlow.collect { isSuccess ->
                _isLoading.value = false
                if (isSuccess) {
                    _errorMessage.value = "Lưu thành công!"
                } else {
                    _errorMessage.value = "Lưu thất bại. Vui lòng kiểm tra lại!"
                }
            }
        }
    }
    fun onDeleteProduct() {
        viewModelScope.launch {
            _isLoading.value = true
            val productId = _productId.value ?: return@launch
            repository.adminDeleteProduct(productId).collect { isSuccess ->
                _isLoading.value = false
                if (isSuccess) {
                    _errorMessage.value = "Xóa thành công!"
                } else {
                    _errorMessage.value = "Xóa thất bại. Vui lòng thử lại!"
                }
            }
        }
    }
}
