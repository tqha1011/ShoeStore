package com.example.shoestoreapp.features.user.product.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestoreapp.features.user.cart.data.remote.CartItemResponseDto
import com.example.shoestoreapp.features.user.cart.data.repositories.CartRepository
import com.example.shoestoreapp.features.user.cart.data.repositories.CartRepositoryImpl
import com.example.shoestoreapp.features.user.product.data.models.Product
import com.example.shoestoreapp.features.user.product.data.models.ProductVariant
import com.example.shoestoreapp.features.user.product.data.repositories.ProductRepository
import com.example.shoestoreapp.features.user.product.data.repositories.ProductRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

sealed interface AddToCartUiState {
    data object Idle : AddToCartUiState
    data object Loading : AddToCartUiState
    data class Success(val item: CartItemResponseDto) : AddToCartUiState
    data class Error(val message: String) : AddToCartUiState
}

class ProductDetailViewModel(
    private val productRepository: ProductRepository = ProductRepositoryImpl(),
    private val cartRepository: CartRepository = CartRepositoryImpl()
) : ViewModel() {

    // ============ STATE QUẢN LÝ THÔNG TIN SẢN PHẨM ============
    private val _productDetail = MutableStateFlow<Product?>(null)
    val productDetail = _productDetail.asStateFlow()

    // ============ STATE QUẢN LÝ MÀU VÀ HÌNH ẢNH ĐƯỢC CHỌN ============
    private val _selectedColor = MutableStateFlow<String?>(null)
    val selectedColor = _selectedColor.asStateFlow()

    private val _selectedImageUrl = MutableStateFlow<String?>(null)
    val selectedImageUrl = _selectedImageUrl.asStateFlow()

    // ============ STATE QUẢN LÝ SIZE ĐƯỢC CHỌN ============
    private val _selectedSize = MutableStateFlow<Int?>(null)

    // ============ STATE QUẢN LÝ TRẠNG THÁI LOADING ============
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // ============ STATE QUẢN LÝ TRẠNG THÁI MỞ/RỘNG CÁC SECTION ============
    private val _isShippingExpanded = MutableStateFlow(false)
    val isShippingExpanded = _isShippingExpanded.asStateFlow()

    private val _isDescriptionExpanded = MutableStateFlow(false)
    val isDescriptionExpanded = _isDescriptionExpanded.asStateFlow()

    private val _addToCartState = MutableStateFlow<AddToCartUiState>(AddToCartUiState.Idle)
    val addToCartState = _addToCartState.asStateFlow()

    // Banner properties
    private val _bannerMessage = MutableStateFlow("")
    val bannerMessage = _bannerMessage.asStateFlow()

    private val _isBannerSuccess = MutableStateFlow(true)
    val isBannerSuccess = _isBannerSuccess.asStateFlow()

    private val _showBanner = MutableStateFlow(false)
    val showBanner = _showBanner.asStateFlow()

    // ============ HÀM LOAD CHI TIẾT SẢN PHẨM ============
    fun loadProductDetail(productGuid: String, passedColorName: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val product = productRepository.getProductDetail(productGuid).firstOrNull()

                if (product != null) {
                    _productDetail.value = product

                    if (product.variants.isNotEmpty()) {
                        val matchedVariant = if (!passedColorName.isNullOrEmpty()) {
                            product.variants.firstOrNull {
                                it.colorName?.trim()?.equals(passedColorName.trim(), ignoreCase = true) == true
                            }
                        } else null

                        val defaultVariant = matchedVariant ?: product.variants.first()

                        _selectedColor.value = defaultVariant.colorName?.trim()
                        _selectedImageUrl.value = defaultVariant.imageUrl
                    }
                } else {
                    _bannerMessage.value = "Product details not found."
                    _isBannerSuccess.value = false
                    _showBanner.value = true
                }
            } catch (e: Exception) {
                _bannerMessage.value = e.message ?: "Failed to load product details."
                _isBannerSuccess.value = false
                _showBanner.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ============ HÀM CẬP NHẬT MÀU VÀ TỰ ĐỘNG ĐỔI ẢNH ============
    fun selectColor(colorName: String) {
        _selectedColor.value = colorName

        val currentProduct = _productDetail.value
        if (currentProduct != null) {
            val matchedVariant = currentProduct.variants.firstOrNull { variant ->
                variant.colorName?.trim().equals(colorName.trim(), ignoreCase = true)
            }
            if (matchedVariant != null && matchedVariant.imageUrl?.isNotEmpty() == true) {
                _selectedImageUrl.value = matchedVariant.imageUrl
            }
        }
    }

    // ============ HÀM CẬP NHẬT SIZE ĐƯỢC CHỌN ============
    fun selectSize(size: Int) {
        _selectedSize.value = size
    }

    fun hideBanner() {
        _showBanner.value = false
    }

    private fun isVariantAvailable(variant: ProductVariant): Boolean {
        return variant.isSelling && !variant.isDelete && variant.stock > 0
    }

    fun findSelectedVariant(
        product: Product?,
        selectedColor: String?,
        selectedSize: Int?
    ): ProductVariant? {
        if (product == null) return null
        val normalizedSelectedColor = selectedColor?.trim()?.lowercase()
        if (normalizedSelectedColor.isNullOrEmpty() || selectedSize == null) return null

        return product.variants.firstOrNull { variant ->
            val normalizedVariantColor = variant.colorName?.trim()?.lowercase()
            normalizedVariantColor == normalizedSelectedColor &&
                    variant.size == selectedSize &&
                    isVariantAvailable(variant)
        }
    }

    // ============ HÀM THÊM VÀO GIỎ HÀNG ============
    fun addCartItem(variantPublicId: String, quantity: Int) {
        viewModelScope.launch {
            _addToCartState.value = AddToCartUiState.Loading
            val result = cartRepository.addToCart(variantPublicId, quantity)

            result.onSuccess { cartItem ->
                _addToCartState.value = AddToCartUiState.Success(cartItem)
                _bannerMessage.value = "Added to cart successfully"
                _isBannerSuccess.value = true
                _showBanner.value = true
            }.onFailure { throwable ->
                val errorMsg = throwable.message ?: "Unable to add item to cart."
                _addToCartState.value = AddToCartUiState.Error(errorMsg)
                _bannerMessage.value = errorMsg
                _isBannerSuccess.value = false
                _showBanner.value = true
            }
        }
    }

    fun resetAddToCartState() {
        _addToCartState.value = AddToCartUiState.Idle
    }

    // ============ HÀM TOGGLE EXPANDED SECTIONS ============
    fun toggleShippingExpanded() {
        _isShippingExpanded.value = !_isShippingExpanded.value
    }

    fun toggleDescriptionExpanded() {
        _isDescriptionExpanded.value = !_isDescriptionExpanded.value
    }
}