package com.example.shoestoreapp.features.product.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestoreapp.features.product.data.models.Product
import com.example.shoestoreapp.features.product.data.repositories.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * ProductListViewModel: ViewModel quản lý logic và state cho màn hình danh sách sản phẩm
 *
 * Chức năng:
 * - Lấy dữ liệu sản phẩm từ Repository
 * - Xử lý các action như: favorite, thêm giỏ hàng
 * - Quản lý state của UI (loading, error, data)
 *
 * @param productRepository - Repository cung cấp dữ liệu sản phẩm
 */
class ProductListViewModel(
    private val productRepository: ProductRepository = ProductRepository()
) : ViewModel() {

    // ============ STATE QUẢN LÝ DANH SÁCH SẢN PHẨM ============
    /**
     * productList: Flow của danh sách sản phẩm
     * - Flow là Kotlin Coroutines stream - phát ra giá trị mới khi dữ liệu thay đổi
     * - UI sẽ tự động update khi dữ liệu trong Flow thay đổi
     */
    val productList: Flow<List<Product>> = productRepository.getAllProducts()

    // ============ HÀM TOGGLE FAVORITE ============
    /**
     * Đánh dấu hoặc bỏ đánh dấu sản phẩm yêu thích
     *
     * @param productId - ID của sản phẩm cần toggle
     *
     * Chi tiết hoạt động:
     * - viewModelScope.launch -> Chạy trong background thread (không block UI)
     * - productRepository.toggleFavorite(productId) -> Gọi hàm toggle ở Repository
     * - UI sẽ tự động cập nhật vì productList là Flow
     */
    fun toggleFavorite(productId: Int) {
        viewModelScope.launch {
            productRepository.toggleFavorite(productId)
        }
    }

    // ============ HÀM THÊM VÀO GIỎ HÀNG ============
    /**
     * Thêm sản phẩm vào giỏ hàng
     *
     * @param productId - ID của sản phẩm cần thêm
     */
    fun addToCart(productId: Int) {
        viewModelScope.launch {
            productRepository.addToCart(productId)
        }
    }

    // ============ HÀM FILTER SẢN PHẨM ============
    /**
     * Lọc danh sách sản phẩm theo bộ lọc
     *
     * @param filter - Bộ lọc được chọn (VD: "Air Max", "All Shoes")
     *
     * TODO: Implement logic để filter sản phẩm
     * - Nếu filter = "All Shoes" -> trả về tất cả sản phẩm
     * - Nếu filter = "Air Max" -> lọc chỉ sản phẩm Air Max
     * - Có thể gọi API để filter, hoặc filter local data
     */
    fun filterProducts(filter: String) {
        viewModelScope.launch {
            // TODO: Implement filter logic
            // repository.filterProducts(filter)
            println("Filtering products by: $filter")
        }
    }

    // ============ HÀM TÌM KIẾM SẢN PHẨM ============
    /**
     * Tìm kiếm sản phẩm theo tên
     *
     * @param searchQuery - Từ khóa tìm kiếm
     *
     * TODO: Implement logic để search sản phẩm
     * - Tìm kiếm trong tên sản phẩm
     * - Có thể gọi API search, hoặc search local data
     */
    fun searchProducts(searchQuery: String) {
        viewModelScope.launch {
            if (searchQuery.isNotEmpty()) {
                // TODO: Implement search logic
                // repository.searchProducts(searchQuery)
                println("Searching for: $searchQuery")
            }
        }
    }
}
