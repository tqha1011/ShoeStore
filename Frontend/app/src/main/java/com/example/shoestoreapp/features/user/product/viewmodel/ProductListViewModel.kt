package com.example.shoestoreapp.features.user.product.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestoreapp.features.user.product.data.models.Product
import com.example.shoestoreapp.features.user.product.data.repositories.ProductRepository
import com.example.shoestoreapp.features.user.product.ui.components.BottomNavTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
/**
 * ViewModel cho Product List Screen
 * 
 * Chức năng:
 * - Quản lý state UI (products, filter, search, loading)
 * - Xử lý business logic (filter & search)
 * - Kết nối giữa UI và Repository
 */
class ProductListViewModel(
    private val repository: ProductRepository = ProductRepository()
) : ViewModel() {
    // ============ STATE ============
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val productList: StateFlow<List<Product>> = _products.asStateFlow()
    private val _selectedFilter = MutableStateFlow("All Shoes")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()
    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()
    
    private val _selectedBottomTab = MutableStateFlow(BottomNavTab.SHOP)
    val selectedBottomTab: StateFlow<BottomNavTab> = _selectedBottomTab.asStateFlow()

    // ============ INIT ============
    init {
        loadProducts()
    }
    // ============ LOAD DATA ============
    private fun loadProducts() {
        viewModelScope.launch {
            _products.value = repository.getAllProducts().value
        }
    }
    // ============ CALLBACKS ============
    /**
     * Xử lý khi user chọn filter
     */
    fun onFilterSelected(filter: String) {
        _selectedFilter.value = filter
        applyFiltersAndSearch()
    }
    
    /**
     * Xử lý khi user gõ text tìm kiếm
     */
    fun onSearchChanged(query: String) {
        _searchText.value = query
        applyFiltersAndSearch()
    }
    
    /**
     * Xử lý khi user chọn tab ở BottomNavBar
     * 
     * @param tab - Tab được chọn (SHOP, DISCOVER, HOME, FAVORITES, PROFILE)
     */
    fun onTabSelected(tab: BottomNavTab) {
        _selectedBottomTab.value = tab
    }
    // ============ BUSINESS LOGIC ============
    /**
     * Áp dụng filter và search, cập nhật danh sách hiển thị
     */
    private fun applyFiltersAndSearch() {
        val filtered = getFilteredProducts()
        val searched = searchProducts(_searchText.value)
        _products.value = if (_searchText.value.isEmpty()) {
            filtered
        } else {
            filtered.filter { product ->
                searched.any { it.id == product.id }
            }
        }
    }
    /**
     * Lọc sản phẩm theo filter được chọn
     */
    private fun getFilteredProducts(): List<Product> {
        val allProducts = repository.getAllProducts().value
        return when (_selectedFilter.value) {
            "Air Max" -> allProducts.filter { it.category == "Air Max" }
            "Dunk" -> allProducts.filter { it.category == "Dunk" }
            "Pegasus" -> allProducts.filter { it.category == "Pegasus" }
            "Jordan" -> allProducts.filter { it.category == "Jordan" }
            else -> allProducts
        }
    }
    /**
     * Tìm kiếm sản phẩm theo tên
     */
    private fun searchProducts(query: String): List<Product> {
        return if (query.isEmpty()) {
            repository.getAllProducts().value
        } else {
            repository.getAllProducts().value.filter { product ->
                product.name.contains(query, ignoreCase = true)
            }
        }
    }
    /**
     * Xử lý khi user click yêu thích sản phẩm
     * 
     * Logic:
     * - Gọi repository để đảo trạng thái isFavorite (data logic)
     * - Cập nhật state UI local (_products)
     * 
     * Note: Data logic (đảo isFavorite) ở Repository, UI state update ở ViewModel
     * 
     * @param productId - ID sản phẩm
     */
    fun toggleFavorite(productId: Int) {
        // Gọi repository để xử lý data logic
        val updatedProduct = repository.toggleFavorite(productId)
        
        // Nếu thành công → cập nhật local state UI
        if (updatedProduct != null) {
            val updatedProducts = _products.value.map { product ->
                if (product.id == productId) updatedProduct else product
            }
            _products.value = updatedProducts
        }
    }
}
