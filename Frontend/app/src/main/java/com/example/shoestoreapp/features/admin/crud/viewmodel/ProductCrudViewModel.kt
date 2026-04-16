package com.example.shoestoreapp.features.admin.crud.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestoreapp.core.utils.Resource
import com.example.shoestoreapp.features.admin.crud.data.remote.ProductCreateDtoRequest
import com.example.shoestoreapp.features.admin.crud.data.remote.ProductCreateVariantDto
import com.example.shoestoreapp.features.admin.crud.data.remote.ProductUpdateDtoRequest
import com.example.shoestoreapp.features.admin.crud.data.remote.ProductUpdateVariantDto
import com.example.shoestoreapp.features.admin.crud.data.remote.master_data.CategoryDto
import com.example.shoestoreapp.features.admin.crud.data.remote.master_data.ColorDto
import com.example.shoestoreapp.features.admin.crud.data.remote.master_data.SizeDto
import com.example.shoestoreapp.features.admin.crud.data.repositories.MasterDataRepository
import com.example.shoestoreapp.features.admin.crud.data.repositories.ProductCrudRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductCrudViewModel(
    private val repository: ProductCrudRepository,
    private val masterDataRepo: MasterDataRepository
) : ViewModel() {

    // --- MASTER DATA ---
    private val _sizesList = MutableStateFlow<List<SizeDto>>(emptyList())
    val sizesList = _sizesList.asStateFlow()

    private val _colorsList = MutableStateFlow<List<ColorDto>>(emptyList())
    val colorsList = _colorsList.asStateFlow()

    private val _categoriesList = MutableStateFlow<List<CategoryDto>>(emptyList())
    val categoriesList = _categoriesList.asStateFlow()

    // --- FORM STATES ---
    private val _productName = MutableStateFlow("")
    val productName = _productName.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow("")
    val selectedCategoryId = _selectedCategoryId.asStateFlow()

    private val _selectedCategoryName = MutableStateFlow("Chọn loại giày")
    val selectedCategoryName = _selectedCategoryName.asStateFlow()

    private val _price = MutableStateFlow(0.0)
    val price = _price.asStateFlow()

    private val _stock = MutableStateFlow(0)
    val stock = _stock.asStateFlow()

    private val _selectedSizeId = MutableStateFlow("")
    val selectedSizeId = _selectedSizeId.asStateFlow()

    private val _selectedSizeValue = MutableStateFlow("Chọn Size")
    val selectedSizeValue = _selectedSizeValue.asStateFlow()

    private val _selectedColorId = MutableStateFlow("")
    val selectedColorId = _selectedColorId.asStateFlow()

    private val _selectedColorName = MutableStateFlow("Chọn màu")
    val selectedColorName = _selectedColorName.asStateFlow()

    private val _imageUrl = MutableStateFlow<String?>(null)
    val imageUrl = _imageUrl.asStateFlow()

    private val _productId = MutableStateFlow<String?>(null)
    val productId = _productId.asStateFlow()

    // --- STATUS ---
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()


    init {
        fetchMasterData()
    }

    private fun fetchMasterData() {
        viewModelScope.launch {
            launch { masterDataRepo.getSizes().collect { res -> if (res is Resource.Success) _sizesList.value = res.data } }
            launch { masterDataRepo.getColors().collect { res -> if (res is Resource.Success) _colorsList.value = res.data } }
            launch { masterDataRepo.getCategories().collect { res -> if (res is Resource.Success) _categoriesList.value = res.data } }
        }
    }

    // --- HANDLERS ---
    fun onProductNameChange(newName: String) { _productName.value = newName }
    fun onPriceChange(newPrice: Double) { _price.value = newPrice }
    fun onStockChange(newStock: Int) { _stock.value = newStock }
    fun onImageUrlChange(newUrl: String) { _imageUrl.value = newUrl }
    fun onProductIdChange(newId: String?) { _productId.value = newId }
    fun clearErrorMessage() { _errorMessage.value = null }


    fun onCategorySelected(id: String, name: String) {
        _selectedCategoryId.value = id
        _selectedCategoryName.value = name
    }

    fun onSizeSelected(id: String, value: String) {
        _selectedSizeId.value = id
        _selectedSizeValue.value = value
    }

    fun onColorSelected(id: String, name: String) {
        _selectedColorId.value = id
        _selectedColorName.value = name
    }

    // --- LOGIC LƯU SẢN PHẨM ---
    fun onSaveProduct() {
        viewModelScope.launch {
            val currentId = _productId.value

            if (currentId == null) {
                // 1. Logic TẠO MỚI
                val createRequest = ProductCreateDtoRequest(
                    productName = _productName.value,
                    brand = "Nike",
                    categoryId = _selectedCategoryId.value,
                    variants = listOf(
                        ProductCreateVariantDto(
                            sizeId = _selectedSizeId.value.toIntOrNull() ?: 0,
                            size = _selectedSizeValue.value.toIntOrNull() ?: 0,
                            colorId = _selectedColorId.value.toIntOrNull() ?: 0,
                            colorName = _selectedColorName.value,
                            stock = _stock.value,
                            price = _price.value,
                            imageUrl = _imageUrl.value ?: "",
                            isSelling = true
                        )
                    )
                )
                repository.adminCreateProduct(createRequest).collect { resource ->
                    handleResource(resource, successMsg = "Đã thêm sản phẩm mới thành công!")
                }
            } else {
                // 2. Logic CẬP NHẬT
                val updateRequest = ProductUpdateDtoRequest(
                    productName = _productName.value,
                    brand = "Nike",
                    categoryId = _selectedCategoryId.value,
                    variants = listOf(
                        ProductUpdateVariantDto(
                            publicId = null,
                            sizeId = _selectedSizeId.value.toIntOrNull() ?: 0,
                            colorId = _selectedColorId.value.toIntOrNull() ?: 0,
                            stock = _stock.value,
                            price = _price.value,
                            imageUrl = _imageUrl.value,
                            isSelling = true
                        )
                    )
                )
                repository.adminUpdateProduct(currentId, updateRequest).collect { resource ->
                    handleResource(resource, successMsg = "Cập nhật thông tin sản phẩm thành công!")
                }
            }
        }
    }

    // --- LOGIC XÓA SẢN PHẨM ---
    fun onDeleteProduct() {
        val id = _productId.value ?: return
        viewModelScope.launch {
            repository.adminDeleteProduct(id).collect { resource ->
                handleResource(resource, successMsg = "Sản phẩm đã được xóa khỏi hệ thống!")
            }
        }
    }

    /**
     * Hàm dùng chung để xử lý Resource truyền từ Repository về
     * @param resource: Dữ liệu bọc trong Resource (Loading, Success, Error)
     * @param successMsg: Câu thông báo riêng biệt cho từng hành động
     */
    private fun handleResource(resource: Resource<Unit>, successMsg: String) {
        when (resource) {
            is Resource.Loading -> {
                _isLoading.value = true
                _errorMessage.value = null
            }
            is Resource.Success -> {
                _isLoading.value = false
                _errorMessage.value = successMsg // In ra thông báo thành công riêng biệt
            }
            is Resource.Error -> {
                _isLoading.value = false
                _errorMessage.value = resource.message // In ra lỗi từ Backend (400, 500...)
            }
        }
    }
}