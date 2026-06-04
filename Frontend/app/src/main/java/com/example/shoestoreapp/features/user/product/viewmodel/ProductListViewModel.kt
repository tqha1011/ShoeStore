package com.example.shoestoreapp.features.user.product.viewmodel
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

    private val _isLastPage = MutableStateFlow(false)
    val isLastPage: StateFlow<Boolean> = _isLastPage.asStateFlow()

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
     * @param filter - Tên filter được chọn
     */
    fun onFilterSelected(filter: String) {
        _selectedFilter.value = filter
        applyFiltersAndSearch()
    }

    /**
     * Xử lý khi user gõ text tìm kiếm
     * @param query - Từ khóa tìm kiếm
     */
    fun onSearchChanged(query: String) {
        _searchText.value = query
        applyFiltersAndSearch()
    }

    /**
     * Xử lý khi user chọn tab ở BottomNavBar
     */
    fun onTabSelected(tab: BottomNavTab) {
        _selectedBottomTab.value = tab
    }

    /**
     * Load trang tiếp theo (pagination)
     */
    fun loadNextPage() {
        if (_isLoadingMore.value || _isLastPage.value) {
            return
        }

        viewModelScope.launch {
            _isLoadingMore.value = true
            try {
                val nextPage = _currentPage.value + 1

                // Lấy ID chuẩn xác từ hàm helper
                val categoryIdFilter = getCategoryIdFromName(_selectedFilter.value)

                val request = ProductSearchRequest(
                    keyword = _searchText.value.ifEmpty { null },
                    categoryId = categoryIdFilter,
                    pageIndex = nextPage,
                    pageSize = DEFAULT_PAGE_SIZE
                )

                val products = repository.searchProducts(request).first()

                if (products.isNullOrEmpty()) {
                    _isLastPage.value = true
                } else {
                    _products.value = _products.value?.plus(products)
                    _currentPage.value = nextPage
                    _errorMessage.value = null

                    if (products.size < DEFAULT_PAGE_SIZE) {
                        _isLastPage.value = true
                    }
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
     */
    private fun applyFiltersAndSearch() {
        viewModelScope.launch {
            _isLoading.value = true
            _isLastPage.value = false

            val searchTerm = searchText.value.ifEmpty { null }

            val categoryIdFilter = getCategoryIdFromName(_selectedFilter.value)

            val request = ProductSearchRequest(
                keyword = searchTerm,
                categoryId = categoryIdFilter,
                pageIndex = 1,
                pageSize = DEFAULT_PAGE_SIZE
            )

            repository.searchProducts(request).collect { products ->
                _products.value = products
                _currentPage.value = 1

                if (products.isNullOrEmpty() || products.size < DEFAULT_PAGE_SIZE) {
                    _isLastPage.value = true
                }

                _isLoading.value = false
            }
        }
    }

    // ============ HELPER ============
    /**
     * Map tên category trên UI về đúng ID dưới database
     */
    private fun getCategoryIdFromName(name: String): String? {
        return when (name) {
            "Running" -> "1"
            "Men's shoes" -> "2"
            "Women's shoes" -> "3"
            "Kid's shoes" -> "4"
            else -> null // Tương đương với "All Shoes"
        }
    }
}
