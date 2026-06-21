package com.example.shoestoreapp.features.admin.product.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestoreapp.features.admin.product.data.models.AdminProduct
import com.example.shoestoreapp.features.admin.product.data.repositories.AdminProductRepository
import com.example.shoestoreapp.features.admin.product.data.repositories.AdminProductRepositoryImpl
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    private val repository: AdminProductRepository = AdminProductRepositoryImpl()
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

    private val _currentPage = MutableStateFlow(1)

    private val _hasNext = MutableStateFlow(false)
    val hasNext: StateFlow<Boolean> = _hasNext.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private var listRequestJob: Job? = null

    // ============ INIT ============
    init {
        loadProducts()
    }
    // ============ LOAD DATA ============
    fun loadProducts() {
        listRequestJob?.cancel()
        listRequestJob = viewModelScope.launch {
            _isLoading.value = true
            _currentPage.value = 1
            _hasNext.value = false
            _products.value = emptyList()
            try {
                repository.searchProducts(
                    keyword = null,
                    inStock = null,
                    outOfStock = null,
                    lowStock = null,
                    pageIndex = 1,
                    pageSize = 6
                ).collect { page ->
                    _products.value = page.items
                    _currentPage.value = page.pageNumber
                    _hasNext.value = page.hasNext
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadNextPage() {
        if (_isLoading.value || _isLoadingMore.value || !_hasNext.value) {
            return
        }

        viewModelScope.launch {
            _isLoadingMore.value = true
            try {
                val nextPage = _currentPage.value + 1

                val keyword = _searchText.value.ifEmpty { null }
                val inStock = (_selectedFilter.value == "IN STOCK").takeIf { it }
                val outOfStock = (_selectedFilter.value == "OUT OF STOCK").takeIf { it }
                val lowStock = (_selectedFilter.value == "LOW STOCK").takeIf { it }
                val pageIndex = nextPage
                val pageSize = 6

                val page = repository.searchProducts(
                    keyword,
                    inStock,
                    outOfStock,
                    lowStock,
                    pageIndex,
                    pageSize
                ).first()

                if (page.items.isNotEmpty()) {
                    _products.value = _products.value + page.items
                    _currentPage.value = page.pageNumber
                }
                _hasNext.value = page.hasNext
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
        if (_selectedFilter.value == filter) return
        _selectedFilter.value = filter
        applyFiltersAndSearch()
    }
    /**
     * Xử lý khi user gõ text tìm kiếm
     * 
     * @param query - Text tìm kiếm
     */
    fun onSearchChanged(query: String) {
        if (_searchText.value == query) return
        _searchText.value = query
        applyFiltersAndSearch(debounceMillis = 300)
    }
    // ============ BUSINESS LOGIC ============
    /**
     * Áp dụng filter và search, cập nhật danh sách hiển thị
     */
    private fun applyFiltersAndSearch(debounceMillis: Long = 0L) {
        listRequestJob?.cancel()
        listRequestJob = viewModelScope.launch {
            if (debounceMillis > 0) {
                delay(debounceMillis)
            }

            _currentPage.value = 1
            _hasNext.value = false

            val keyword = _searchText.value.ifEmpty { null }
            val inStock = (_selectedFilter.value == "IN STOCK").takeIf { it }
            val outOfStock = (_selectedFilter.value == "OUT OF STOCK").takeIf { it }
            val lowStock = (_selectedFilter.value == "LOW STOCK").takeIf { it }
            val pageIndex = 1
            val pageSize = 6

            val page = repository.searchProducts(
                keyword,
                inStock,
                outOfStock,
                lowStock,
                pageIndex,
                pageSize
            ).first()
            _products.value = page.items
            _currentPage.value = page.pageNumber
            _hasNext.value = page.hasNext
        }
    }

}