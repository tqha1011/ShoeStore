package com.example.shoestoreapp.features.admin.product.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestoreapp.features.admin.product.data.models.AdminProduct
import com.example.shoestoreapp.features.admin.product.data.repositories.AdminProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
    private val _products = MutableStateFlow<List<AdminProduct>?>(emptyList())
    val products: StateFlow<List<AdminProduct>?> = _products.asStateFlow()
    private val _selectedFilter = MutableStateFlow("ALL PRODUCTS")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()
    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentPage = MutableStateFlow(1)

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()
    // ============ INIT ============
    init {
        loadProducts()
    }
    // ============ LOAD DATA ============
    private fun loadProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.searchProducts(
                    keyword = null,
                    inStock = false,
                    outOfStock = false,
                    lowStock = false,
                    pageIndex = 1,
                    pageSize = 4
                ).collect { products ->
                    _products.value = products ?: emptyList()
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadNextPage() {
        // Prevent duplicate requests - if already loading more, ignore
        if (_isLoadingMore.value) {
            return
        }

        viewModelScope.launch {
            _isLoadingMore.value = true
            try {
                val nextPage = _currentPage.value + 1

                val keyword = _searchText.value.ifEmpty { null }
                val inStock = if (_selectedFilter.value == "IN STOCK") true else null
                val outOfStock = if (_selectedFilter.value == "OUT OF STOCK") true else null
                val lowStock = if (_selectedFilter.value == "LOW STOCK") true else null
                val pageIndex = nextPage + 1
                val pageSize = 4



                val products = repository.searchProducts(keyword,inStock,outOfStock,lowStock,
                    pageIndex, pageSize).first()

                // Thêm sản phẩm mới vào danh sách hiện tại (append, không replace)
                if (products?.isNotEmpty() ?: false) {
                    _products.value = _products.value?.plus(products)
                    _currentPage.value = nextPage
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoadingMore.value = false
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
        viewModelScope.launch{
            val keyword = _searchText.value.ifEmpty {null}
            val inStock = if (_selectedFilter.value == "IN STOCK") true else null
            val outOfStock = if (_selectedFilter.value == "OUT OF STOCK") true else null
            val lowStock = if (_selectedFilter.value == "LOW STOCK") true else null
            val pageIndex = 1
            val pageSize = 4
            repository.searchProducts(keyword, inStock, outOfStock,lowStock,pageIndex, pageSize).collect {
                products ->
                _products.value = products ?: emptyList()
                _currentPage.value = 1
                _isLoading.value = false
            }
        }
    }

}
