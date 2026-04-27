package com.example.shoestoreapp.features.user.product.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestoreapp.features.user.cart.data.remote.CartItemResponseDto
import com.example.shoestoreapp.features.user.cart.data.repositories.CartRepository
import com.example.shoestoreapp.features.user.cart.data.repositories.CartRepositoryImpl
import com.example.shoestoreapp.features.user.product.data.models.Product
import com.example.shoestoreapp.features.user.product.data.models.ProductVariant
import com.example.shoestoreapp.features.user.product.data.repositories.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
sealed interface AddToCartUiState {
    data object Idle : AddToCartUiState
    data object Loading : AddToCartUiState
    data class Success(val item: CartItemResponseDto) : AddToCartUiState
    data class Error(val message: String) : AddToCartUiState
}



/**
 * ProductDetailViewModel: ViewModel quản lý logic và state cho màn hình chi tiết sản phẩm
 *
 * Chức năng:
 * - Lấy thông tin chi tiết sản phẩm từ Repository
 * - Xử lý các action: favorite, thêm giỏ hàng, chọn size
 * - Quản lý state của UI (loading, error, selected size)
 *
 * @param productRepository - Repository cung cấp dữ liệu sản phẩm
 */
class ProductDetailViewModel(
    private val productRepository: ProductRepository = ProductRepository(),
    private val cartRepository: CartRepository = CartRepositoryImpl()
) : ViewModel() {

    // ============ STATE QUẢN LÝ THÔNG TIN SẢN PHẨM ============
    /**
     * _productDetail: MutableStateFlow chứa thông tin chi tiết sản phẩm hiện tại
     * - Dùng private để không cho bên ngoài sửa trực tiếp
     * - Bên trong ViewModel có thể update giá trị
     */
    private val _productDetail = MutableStateFlow<Product?>(null)

    /**
     * productDetail: Public flow - UI observe flow này để cập nhật giao diện
     */
    val productDetail = _productDetail.asStateFlow()

    // ============ STATE QUẢN LÝ SIZE ĐƯỢC CHỌN ============
    /**
     * _selectedSize: MutableStateFlow chứa size giày được chọn
     * - Ban đầu là null (chưa chọn size)
     * - Khi user click vào size button, giá trị sẽ cập nhật
     */
    private val _selectedSize = MutableStateFlow<Int?>(null)


    // ============ STATE QUẢN LÝ TRẠNG THÁI LOADING ============
    /**
     * _isLoading: MutableStateFlow chứa trạng thái đang tải dữ liệu
     * - true: Đang tải thông tin sản phẩm
     * - false: Đã tải xong
     */
    private val _isLoading = MutableStateFlow(false)

    /**
     * isLoading: Public flow - UI observe để hiển thị/ẩn loading indicator
     */
    val isLoading = _isLoading.asStateFlow()

    // ============ STATE QUẢN LÝ TRẠNG THÁI MỞ/RỘNG CÁC SECTION ============
    /**
     * _isShippingExpanded: MutableStateFlow chứa trạng thái mở/rộng của section Shipping & Returns
     * - true: Đang mở
     * - false: Đang đóng
     */
    private val _isShippingExpanded = MutableStateFlow(false)

    /**
     * isShippingExpanded: Public flow - UI observe để biết section Shipping & Returns đang mở hay đóng
     */
    val isShippingExpanded = _isShippingExpanded.asStateFlow()

    /**
     * _isDescriptionExpanded: MutableStateFlow chứa trạng thái mở/rộng của section Product Description
     * - true: Đang mở
     * - false: Đang đóng
     */
    private val _isDescriptionExpanded = MutableStateFlow(false)

    /**
     * isDescriptionExpanded: Public flow - UI observe để biết section Product Description đang mở hay đóng
     */
    val isDescriptionExpanded = _isDescriptionExpanded.asStateFlow()

    private val _addToCartState = MutableStateFlow<AddToCartUiState>(AddToCartUiState.Idle)
    val addToCartState = _addToCartState.asStateFlow()

    // ============ HÀM LOAD CHI TIẾT SẢN PHẨM ============
    /**
     * Tải thông tin chi tiết sản phẩm theo GUID
     *
     * @param productGuid - GUID của sản phẩm cần tải
     *
     * Chi tiết hoạt động:
     * 1. Set _isLoading = true để hiển thị loading screen
     * 2. Lấy chi tiết sản phẩm từ Repository bằng GUID
     * 3. Lưu vào _productDetail
     * 4. Set _isLoading = false khi hoàn thành
     */
    fun loadProductDetail(productGuid: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val product = productRepository.getProductDetail(productGuid).first()
                _productDetail.value = product
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ============ HÀM CẬP NHẬT SIZE ĐƯỢC CHỌN ============
    /**
     * Cập nhật size được chọn khi user click vào size button
     *
     * @param size - Kích thước giày (7, 8, 9, 10, 11, v.v.)
     */
    fun selectSize(size: Int) {
        _selectedSize.value = size
    }

    private fun isVariantAvailable(variant: ProductVariant): Boolean {
        return variant.isSelling && !variant.isDelete && variant.stock > 0
    }

    /**
     * Chọn variant theo màu/size đã chọn.
     * - Nếu chưa chọn đủ màu và size: trả về null để chặn add-to-cart.
     * - So khớp màu theo trim/lowercase để tránh lệch do dữ liệu có khoảng trắng/chữ hoa-thường.
     */
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
    /**
     * Thêm sản phẩm vào giỏ hàng
     *
     * Điều kiện:
     * - Phải chọn size (_selectedSize không được null)
     * - Sản phẩm phải có (productDetail không được null)
     *
     * @param - GUID của sản phẩm cần thêm
     */
    fun addCartItem(variantPublicId: String, quantity: Int) {
        viewModelScope.launch {
            _addToCartState.value = AddToCartUiState.Loading
            val result = cartRepository.addToCart(variantPublicId, quantity)

            result.onSuccess { cartItem ->
                _addToCartState.value = AddToCartUiState.Success(cartItem)
            }.onFailure { throwable ->
                _addToCartState.value = AddToCartUiState.Error(
                    throwable.message ?: "Unable to add item to cart."
                )
            }
        }
    }

    fun addToCart(variantId: String, quantity: Int) {
        addCartItem(variantId, quantity)
    }

    fun resetAddToCartState() {
        _addToCartState.value = AddToCartUiState.Idle
    }


    // ============ HÀM TOGGLE EXPANDED SECTIONS ============
    /**
     * Toggle trạng thái mở/đóng của Shipping & Returns section
     */
    fun toggleShippingExpanded() {
        _isShippingExpanded.value = !_isShippingExpanded.value
    }

    /**
     * Toggle trạng thái mở/đóng của Product Description section
     */
    fun toggleDescriptionExpanded() {
        _isDescriptionExpanded.value = !_isDescriptionExpanded.value
    }
}
