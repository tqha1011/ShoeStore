package com.example.shoestoreapp.features.user.product.viewmodel
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestoreapp.features.user.product.data.models.Product
import com.example.shoestoreapp.features.user.product.data.remote.ProductSearchRequest
import com.example.shoestoreapp.features.user.product.data.repositories.ProductRepository
import com.example.shoestoreapp.features.user.product.ui.components.BottomNavTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * ViewModel cho Product List Screen
 * 
 * Chức năng:
 * - Quản lý state UI (products, filter, search, loading)
 * - Xử lý business logic (filter & search)
 * - Kết nối giữa UI và Repository qua API mới
 * 
 * API được sử dụng:
 * - repository.searchProducts(request) - Tìm kiếm với filters/sorting/pagination
 * - repository.getProductsByCategory(categoryId) - Lọc theo danh mục
 * - repository.updateFavoriteToAPI(productGuid, status) - Cập nhật yêu thích
 */
class ProductListViewModel(
    private val repository: ProductRepository = ProductRepository()
) : ViewModel() {
    // ============ STATE ============
    private val _products = MutableStateFlow<List<Product>?>(emptyList())
    val productList: StateFlow<List<Product>?> = _products.asStateFlow()
    
    private val _selectedFilter = MutableStateFlow("All Shoes")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()
    
    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()
    
    private val _selectedBottomTab = MutableStateFlow(BottomNavTab.SHOP)
    val selectedBottomTab: StateFlow<BottomNavTab> = _selectedBottomTab.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _currentPage = MutableStateFlow(1)

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    companion object {
        private const val DEFAULT_PAGE_SIZE = 4
    }

    // ============ INIT ============
    init {
        loadProducts()
    }

    // ============ LOAD DATA ============
    /**
     * Tải danh sách sản phẩm mặc định từ API
     * Sử dụng: repository.searchProducts() với tham số mặc định
     */
    private fun loadProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.searchProducts(
                ProductSearchRequest(
                    pageIndex = 1,
                    pageSize = DEFAULT_PAGE_SIZE
                )
            ).collect { products ->
                Log.d("SHOE_DEBUG", "Dữ liệu đã về ViewModel: ${products?.size} đôi")
                _products.value = products
                _isLoading.value = false
            }
        }
    }

    // ============ CALLBACKS ============
    /**
     * Xử lý khi user chọn filter
     * Sử dụng: repository.searchProducts() với categoryId filter
     * 
     * @param filter - Tên filter được chọn (VD: "Air Max", "Dunk", "All Shoes")
     */
    fun onFilterSelected(filter: String) {
        _selectedFilter.value = filter
        applyFiltersAndSearch()
    }
    
    /**
     * Xử lý khi user gõ text tìm kiếm
     * Sử dụng: repository.searchProducts() với searchTerm
     * 
     * @param query - Từ khóa tìm kiếm
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

    /**
     * Load trang tiếp theo (pagination)
     * Sử dụng: repository.searchProducts() với pageNumber mới
     * 
     * Gọi tự động khi user scroll đến cuối LazyColumn
     * - Kiểm tra isLoadingMore để tránh duplicate requests
     * - Reset trang về 1 nếu search/filter thay đổi
     */
    fun loadNextPage() {
        // Prevent duplicate requests - if already loading more, ignore
        if (_isLoadingMore.value) {
            return
        }

        viewModelScope.launch {
            _isLoadingMore.value = true
            try {
                val nextPage = _currentPage.value + 1
                val categoryId = getCategoryIdFromFilter(_selectedFilter.value)
                
                val request = ProductSearchRequest(
                    keyword = _searchText.value.ifEmpty { null },
                    brand = categoryId,
                    pageIndex = nextPage,
                    pageSize = DEFAULT_PAGE_SIZE
                )

                val products = repository.searchProducts(request).first()
                
                // Thêm sản phẩm mới vào danh sách hiện tại (append, không replace)
                if (products?.isNotEmpty() ?: false) {
                    _products.value = _products.value?.plus(products)
                    _currentPage.value = nextPage
                    _errorMessage.value = null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "Failed to load more products: ${e.message}"
            } finally {
                _isLoadingMore.value = false
            }
        }
    }

    // ============ BUSINESS LOGIC ============
    /**
     * Áp dụng filter và search, gọi API với parameters tương ứng
     * Sử dụng: repository.searchProducts() với các tham số được xây dựng
     * 
     * Logic:
     * 1. Nếu chỉ có filter (không search): Có thể dùng getProductsByCategory()
     * 2. Nếu có search + filter: Dùng searchProducts() với cả hai
     * 3. Reset về trang 1 khi search/filter thay đổi
     */
    private fun applyFiltersAndSearch() {
        viewModelScope.launch {
            _isLoading.value = true
            val categoryId = getCategoryIdFromFilter(_selectedFilter.value)
            val searchTerm = searchText.value.ifEmpty {null}
            val request = ProductSearchRequest(
                keyword = searchTerm,
                brand = categoryId,
                pageIndex = 1,
                pageSize = DEFAULT_PAGE_SIZE
            )
            repository.searchProducts(request).collect { products ->
                _products.value = products
                _currentPage.value = 1
                _isLoading.value = false
            }
        }
    }

    /**
     * Convert filter name thành categoryId
     * Sử dụng cho repository.searchProducts(categoryId=...)
     * 
     * @param filterName - Tên filter (VD: "Air Max", "Dunk")
     * @return categoryId hoặc null
     */
    private fun getCategoryIdFromFilter(filterName: String): String? {
        return when (filterName) {
            "Air Max" -> "air-max"
            "Dunk" -> "dunk"
            "Pegasus" -> "pegasus"
            "Jordan" -> "jordan"
            else -> null // "All Shoes" → null (không filter)
        }
    }

    /**
     * Xử lý khi user click yêu thích sản phẩm
     * Sử dụng: repository.updateFavoriteToAPI(productGuid, status)
     * 
     * Logic:
     * 1. Gọi API để cập nhật trên backend
     * 2. Cập nhật local state UI
     * 3. Handle errors gracefully
     *
     */
//    fun toggleFavorite(productGuid: String) {
//        viewModelScope.launch {
//
//        }
//    }
}
