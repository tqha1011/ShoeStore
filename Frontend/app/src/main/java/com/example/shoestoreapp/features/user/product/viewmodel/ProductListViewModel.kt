package com.example.shoestoreapp.features.user.product.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestoreapp.features.user.product.data.models.Product
import com.example.shoestoreapp.features.user.product.data.remote.ProductSearchRequest
import com.example.shoestoreapp.features.user.product.data.repositories.ProductRepository
import com.example.shoestoreapp.features.user.product.data.repositories.ProductRepositoryImpl
import com.example.shoestoreapp.features.user.product.ui.components.BottomNavTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ProductListViewModel(
    private val repository: ProductRepository = ProductRepositoryImpl()
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

    private val _currentPage = MutableStateFlow(1)

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _isLastPage = MutableStateFlow(false)
    val isLastPage: StateFlow<Boolean> = _isLastPage.asStateFlow()

    // ============ STATE BANNER BÁO LỖI ĐỒNG BỘ ============
    private val _bannerMessage = MutableStateFlow("")
    val bannerMessage: StateFlow<String> = _bannerMessage.asStateFlow()

    private val _showBanner = MutableStateFlow(false)
    val showBanner: StateFlow<Boolean> = _showBanner.asStateFlow()

    companion object {
        private const val DEFAULT_PAGE_SIZE = 4
    }

    // ============ INIT ============
    init {
        loadProducts()
    }

    // ============ ACTIONS ============
    fun hideBanner() {
        _showBanner.value = false
    }

    // ============ LOAD DATA ============
    private fun loadProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.searchProducts(
                ProductSearchRequest(pageIndex = 1, pageSize = DEFAULT_PAGE_SIZE)
            )
                .catch { e ->
                    _bannerMessage.value = e.message ?: "Failed to load products."
                    _showBanner.value = true
                    _isLoading.value = false
                }
                .collect { products ->
                    _products.value = products
                    _isLoading.value = false
                }
        }
    }

    // ============ CALLBACKS ============
    fun onFilterSelected(filter: String) {
        _selectedFilter.value = filter
        applyFiltersAndSearch()
    }

    fun onSearchChanged(query: String) {
        _searchText.value = query
        applyFiltersAndSearch()
    }

    fun onTabSelected(tab: BottomNavTab) {
        _selectedBottomTab.value = tab
    }

    fun loadNextPage() {
        if (_isLoadingMore.value || _isLastPage.value) return

        viewModelScope.launch {
            _isLoadingMore.value = true
            try {
                val nextPage = _currentPage.value + 1
                val categoryIdFilter = getCategoryIdFromName(_selectedFilter.value)

                val request = ProductSearchRequest(
                    keyword = _searchText.value.ifEmpty { null },
                    categoryId = categoryIdFilter,
                    pageIndex = nextPage,
                    pageSize = DEFAULT_PAGE_SIZE
                )

                val products = repository.searchProducts(request).first()

                if (products.isEmpty()) {
                    _isLastPage.value = true
                } else {
                    _products.value = _products.value?.plus(products)
                    _currentPage.value = nextPage

                    if (products.size < DEFAULT_PAGE_SIZE) {
                        _isLastPage.value = true
                    }
                }
            } catch (e: Exception) {
                _bannerMessage.value = e.message ?: "Failed to load more products."
                _showBanner.value = true
            } finally {
                _isLoadingMore.value = false
            }
        }
    }

    // ============ BUSINESS LOGIC ============
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

            repository.searchProducts(request)
                .catch { e ->
                    _bannerMessage.value = e.message ?: "Failed to search products."
                    _showBanner.value = true
                    _isLoading.value = false
                    _products.value = emptyList()
                }
                .collect { products ->
                    _products.value = products
                    _currentPage.value = 1

                    if (products.isEmpty() || products.size < DEFAULT_PAGE_SIZE) {
                        _isLastPage.value = true
                    }
                    _isLoading.value = false
                }
        }
    }

    // ============ HELPER ============
    private fun getCategoryIdFromName(name: String): String? {
        return when (name) {
            "Running" -> "1"
            "Men's shoes" -> "2"
            "Women's shoes" -> "3"
            "Kid's shoes" -> "4"
            else -> null
        }
    }
}