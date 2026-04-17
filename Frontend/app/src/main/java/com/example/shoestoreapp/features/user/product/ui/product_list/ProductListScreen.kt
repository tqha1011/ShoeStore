package com.example.shoestoreapp.features.user.product.ui.product_list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shoestoreapp.features.user.product.ui.components.BottomNavBar
import com.example.shoestoreapp.features.user.product.ui.components.BottomNavTab
import com.example.shoestoreapp.features.user.product.ui.components.FilterChips
import com.example.shoestoreapp.features.user.product.ui.components.ProductCard
import com.example.shoestoreapp.features.user.product.ui.components.SearchBar
import com.example.shoestoreapp.features.user.product.ui.components.TopAppBar
import com.example.shoestoreapp.features.user.product.viewmodel.ProductListViewModel

/**
 * ProductListScreen: Composable screen hiển thị danh sách sản phẩm với Infinite Scroll
 * 
 * Features:
 * - Search bar + Filter chips
 * - LazyVerticalGrid hiển thị 2 columns
 * - Infinite scroll: Auto load trang tiếp theo khi scroll gần cuối
 * - Loading indicator khi đang load thêm
 * - Error handling
 * 
 * @param viewModel - ViewModel cung cấp dữ liệu và logic
 * @param onNavigateToDetail - Callback khi click vào sản phẩm
 * @param onTopMenuClick - Callback khi click menu
 * @param onNavigateToShoppingBag - Callback khi click shopping bag
 */
@Composable
fun ProductListScreen(
    viewModel: ProductListViewModel,
    onNavigateToDetail: (String) -> Unit = {},  // Changed from Int to String (GUID)
    onTopMenuClick: () -> Unit = {},
    onNavigateToShoppingBag: () -> Unit = {},
    onBottomTabSelected: (BottomNavTab) -> Unit = {}
) {
    val productList = viewModel.productList.collectAsState()
    val selectedFilter = viewModel.selectedFilter.collectAsState()
    val searchText = viewModel.searchText.collectAsState()
    val selectedBottomTab = viewModel.selectedBottomTab.collectAsState()
    val isLoading = viewModel.isLoading.collectAsState()
    val isLoadingMore = viewModel.isLoadingMore.collectAsState()
    val errorMessage = viewModel.errorMessage.collectAsState()

    val lazyGridState = rememberLazyGridState()

    SetupInfiniteScroll(lazyGridState, productList.value, isLoadingMore.value, viewModel)

    Scaffold(
        topBar = {
            TopAppBar(
                onMenuClick = onTopMenuClick,
                onShoppingBagClick = onNavigateToShoppingBag
            )
        },
        bottomBar = {
            BottomNavBar(
                selectedTab = selectedBottomTab.value,
                onTabSelected = { tab ->
                    viewModel.onTabSelected(tab)
                    onBottomTabSelected(tab)
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            SearchBar(
                modifier = Modifier.padding(top = 16.dp),
                searchText = searchText.value,
                onSearchChanged = { query ->
                    viewModel.onSearchChanged(query)
                }
            )

            FilterChipsSection(
                selectedFilter = selectedFilter.value,
                onFilterSelected = { filter ->
                    viewModel.onFilterSelected(filter)
                }
            )

            ErrorMessageBox(errorMessage.value)

            ProductListContent(
                isLoading = isLoading.value,
                productList = productList.value,
                isLoadingMore = isLoadingMore.value,
                lazyGridState = lazyGridState,
                onNavigateToDetail = onNavigateToDetail
            )
        }
    }
}

@Composable
private fun SetupInfiniteScroll(
    lazyGridState: androidx.compose.foundation.lazy.grid.LazyGridState,
    productList: List<*>?,
    isLoadingMore: Boolean,
    viewModel: ProductListViewModel
) {
    LaunchedEffect(lazyGridState) {
        snapshotFlow { lazyGridState.layoutInfo.visibleItemsInfo }
            .collect { visibleItems ->
                if (visibleItems.isNotEmpty()) {
                    val lastVisibleIndex = visibleItems.last().index
                    val totalItems = productList?.size

                    if (lastVisibleIndex >= (totalItems?.minus(2) ?: 0) && !isLoadingMore) {
                        viewModel.loadNextPage()
                    }
                }
            }
    }
}

@Composable
private fun FilterChipsSection(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    Box(modifier = Modifier.padding(vertical = 12.dp)) {
        FilterChips(
            filters = listOf("All Shoes", "Air Max", "Dunk", "Pegasus", "Jordan"),
            selectedFilter = selectedFilter,
            onFilterSelected = onFilterSelected
        )
    }
}

@Composable
private fun ErrorMessageBox(errorMessage: String?) {
    if (!errorMessage.isNullOrEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFCDD2), shape = RoundedCornerShape(4.dp))
                .padding(12.dp)
        ) {
            Text(
                text = errorMessage,
                color = Color(0xFFC62828),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun ProductListContent(
    isLoading: Boolean,
    productList: List<*>?,
    isLoadingMore: Boolean,
    lazyGridState: androidx.compose.foundation.lazy.grid.LazyGridState,
    onNavigateToDetail: (String) -> Unit
) {
    if (isLoading && productList?.isEmpty() == true) {
        LoadingSpinner()
    } else {
        ProductGrid(
            productList = productList,
            isLoadingMore = isLoadingMore,
            lazyGridState = lazyGridState,
            onNavigateToDetail = onNavigateToDetail
        )
    }
}

@Composable
private fun LoadingSpinner() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ProductGrid(
    productList: List<*>?,
    isLoadingMore: Boolean,
    lazyGridState: androidx.compose.foundation.lazy.grid.LazyGridState,
    onNavigateToDetail: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = lazyGridState,
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(productList?.size ?: 0) { index ->
            @Suppress("UNCHECKED_CAST")
            val product = (productList as? List<com.example.shoestoreapp.features.user.product.data.models.Product>)?.getOrNull(index)
            product?.let {
                ProductCard(
                    product = it,
                    onProductClick = { productGuid: String ->
                        onNavigateToDetail(productGuid)
                    },
                    onFavoriteClick = {}
                )
            }
        }

        if (isLoadingMore) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}




