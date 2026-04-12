package com.example.shoestoreapp.features.user.product.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestoreapp.features.user.product.data.models.Product
import com.example.shoestoreapp.features.user.product.data.repositories.ProductRepository
import com.example.shoestoreapp.features.cart.data.repositories.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


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
    private val cartRepository: CartRepository = CartRepository()
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

    /**
     * selectedSize: Public flow - UI observe để biết user chọn size nào
     */
    val selectedSize = _selectedSize.asStateFlow()

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

    // ============ HÀM TOGGLE FAVORITE ============
    /**
     * Đánh dấu hoặc bỏ đánh dấu sản phẩm yêu thích
     */
//    fun toggleFavorite(productGuid: String) {
//        viewModelScope.launch {
//            //
//        }
//    }

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
    fun addToCart(variantId: String, quantity: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                cartRepository.addToCart(variantId, quantity).first()
        } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
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
