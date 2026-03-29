package com.example.shoestoreapp.features.user.product.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestoreapp.features.user.product.data.models.Product
import com.example.shoestoreapp.features.user.product.data.repositories.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * ProductListViewModel: ViewModel quản lý logic cho màn hình danh sách sản phẩm
 *
 * Chức năng:
 * - Lấy danh sách tất cả sản phẩm từ Repository
 * - Quản lý trạng thái tìm kiếm và lọc sản phẩm
 * - Xử lý các action từ user (yêu thích, thêm giỏ hàng)
 *
 * @param productRepository - Repository cung cấp dữ liệu sản phẩm
 */
class ProductListViewModel(
    private val productRepository: ProductRepository = ProductRepository()
) : ViewModel() {

    // ============ STATE QUẢN LÝ DANH SÁCH SẢN PHẨM ============
    /**
     * _productList: MutableStateFlow chứa danh sách sản phẩm hiện tại
     */
    private val _productList = MutableStateFlow<List<Product>>(emptyList())

    /**
     * productList: Public flow - UI observe để cập nhật danh sách
     */
    val productList = _productList.asStateFlow()

    // ============ STATE QUẢN LÝ TÌM KIẾM ============
    /**
     * _searchText: MutableStateFlow chứa nội dung tìm kiếm
     */
    private val _searchText = MutableStateFlow("")

    /**
     * searchText: Public flow - UI observe để cập nhật kết quả tìm kiếm
     */
    val searchText = _searchText.asStateFlow()

    // ============ STATE QUẢN LÝ BỘ LỌC ============
    /**
     * _selectedFilter: MutableStateFlow chứa bộ lọc được chọn
     */
    private val _selectedFilter = MutableStateFlow("All Shoes")

    /**
     * selectedFilter: Public flow - UI observe để cập nhật sản phẩm theo bộ lọc
     */
    val selectedFilter = _selectedFilter.asStateFlow()

    // ============ STATE QUẢN LÝ TRẠNG THÁI LOADING ============
    /**
     * _isLoading: MutableStateFlow chứa trạng thái đang tải
     */
    private val _isLoading = MutableStateFlow(false)

    /**
     * isLoading: Public flow - UI observe để hiển thị loading indicator
     */
    val isLoading = _isLoading.asStateFlow()

    // ============ INIT - LOAD DỮ LIỆU KHI KHỞI TẠO ============
    init {
        loadProducts()
    }

    // ============ HÀM LOAD DANH SÁCH SẢN PHẨM ============
    /**
     * Tải danh sách sản phẩm từ Repository
     *
     * Các bước:
     * 1. Set _isLoading = true
     * 2. Lấy dữ liệu từ productRepository.productList (Flow)
     * 3. Cập nhật _productList
     * 4. Set _isLoading = false
     */
    private fun loadProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                productRepository.productList.collect { products ->
                    _productList.value = products
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ============ HÀM CẬP NHẬT TÌM KIẾM ============
    /**
     * Cập nhật nội dung tìm kiếm
     * @param query - Từ khóa tìm kiếm
     */
    fun onSearchChanged(query: String) {
        _searchText.value = query
    }

    // ============ HÀM CẬP NHẬT BỘ LỌC ============
    /**
     * Cập nhật bộ lọc được chọn
     * @param filter - Tên bộ lọc ("All Shoes", "Air Max", "Dunk", etc.)
     */
    fun onFilterSelected(filter: String) {
        _selectedFilter.value = filter
    }

    // ============ HÀM TOGGLE FAVORITE ============
    /**
     * Đánh dấu hoặc bỏ đánh dấu sản phẩm yêu thích
     * @param productId - ID của sản phẩm
     */
    fun toggleFavorite(productId: Int) {
        viewModelScope.launch {
            productRepository.toggleFavorite(productId)
        }
    }

    // ============ HÀM THÊM VÀO GIỎ HÀNG ============
    /**
     * Thêm sản phẩm vào giỏ hàng
     * @param productId - ID của sản phẩm
     */
    fun addToCart(productId: Int) {
        viewModelScope.launch {
            productRepository.addToCart(productId)
            println("Added product $productId to cart")
        }
    }
}

