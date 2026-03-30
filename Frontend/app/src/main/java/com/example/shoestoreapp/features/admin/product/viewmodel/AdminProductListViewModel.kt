package com.example.shoestoreapp.features.admin.product.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestoreapp.features.admin.product.data.models.AdminProduct
import com.example.shoestoreapp.features.admin.product.data.models.StockStatus
import com.example.shoestoreapp.features.admin.product.data.repositories.AdminProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
/**
 * ViewModel cho Admin Product List Screen
 * 
 * Chức năng:
 * - Quản lý state UI (products, filter, search)
 * - Xử lý business logic (filter & search)
 * - Kết nối giữa UI và Repository
 */
class AdminProductListViewModel(
    private val repository: AdminProductRepository = AdminProductRepository()
) : ViewModel() {
    // ============ STATE ============
    private val _products = MutableStateFlow<List<AdminProduct>>(emptyList())
    val products: StateFlow<List<AdminProduct>> = _products.asStateFlow()
    private val _selectedFilter = MutableStateFlow("ALL PRODUCTS")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()
    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    // ============ INIT ============
    init {
        loadProducts()
    }
    // ============ LOAD DATA ============
    private fun loadProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _products.value = repository.adminProducts.value
            } finally {
                _isLoading.value = false
            }
        }
    }
    // ============ CALLBACKS ============
    /**
     * Xử lý khi user chọn filter
     * 
     * @param filter - Filter được chọn ("ALL PRODUCTS", "IN STOCK", etc)
     */
    fun onFilterChanged(filter: String) {
        _selectedFilter.value = filter
        applyFiltersAndSearch()
    }
    /**
     * Xử lý khi user gõ text tìm kiếm
     * 
     * @param query - Text tìm kiếm
     */
    fun onSearchChanged(query: String) {
        _searchText.value = query
        applyFiltersAndSearch()
    }
    // ============ BUSINESS LOGIC ============
    /**
     * Áp dụng filter và search, cập nhật danh sách hiển thị
     */
    private fun applyFiltersAndSearch() {
        val filtered = getFilteredProducts()
        val searched = searchProducts(_searchText.value)
        _products.value = if (_searchText.value.isEmpty()) {
            // Chỉ dùng filter
            filtered
        } else {
            // Kết hợp filter AND search
            filtered.filter { product ->
                searched.any { it.id == product.id }
            }
        }
    }
    /**
     * Lọc sản phẩm theo filter được chọn
     * 
     * Logic:
     * - "ALL PRODUCTS" → Tất cả sản phẩm
     * - "IN STOCK" → Chỉ sản phẩm còn hàng
     * - "LOW STOCK" → Chỉ sản phẩm sắp hết
     * - "OUT OF STOCK" → Chỉ sản phẩm hết hàng
     * 
     * @return Danh sách sản phẩm sau khi lọc
     */
    private fun getFilteredProducts(): List<AdminProduct> {
        return when (_selectedFilter.value) {
            "IN STOCK" -> repository.adminProducts.value.filter { 
                it.stockStatus == StockStatus.IN_STOCK 
            }
            "LOW STOCK" -> repository.adminProducts.value.filter { 
                it.stockStatus == StockStatus.LOW_STOCK 
            }
            "OUT OF STOCK" -> repository.adminProducts.value.filter { 
                it.stockStatus == StockStatus.OUT_OF_STOCK 
            }
            else -> repository.adminProducts.value  // "ALL PRODUCTS"
        }
    }
    /**
     * Tìm kiếm sản phẩm theo tên
     * 
     * Logic:
     * - Nếu query rỗng → trả về tất cả
     * - Nếu có query → lọc sản phẩm chứa query
     * - Không phân biệt hoa/thường
     * 
     * @param query - Từ khóa tìm kiếm
     * @return Danh sách sản phẩm tìm thấy
     */
    private fun searchProducts(query: String): List<AdminProduct> {
        return if (query.isEmpty()) {
            repository.adminProducts.value
        } else {
            repository.adminProducts.value.filter { product ->
                product.name.contains(query, ignoreCase = true)
            }
        }
    }
}
