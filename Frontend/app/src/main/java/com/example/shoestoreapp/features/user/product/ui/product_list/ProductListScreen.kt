package com.example.shoestoreapp.features.user.product.ui.product_list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.shoestoreapp.features.user.product.ui.components.BottomNavBar
import com.example.shoestoreapp.features.user.product.ui.components.BottomNavTab
import com.example.shoestoreapp.features.user.product.ui.components.FilterChips
import com.example.shoestoreapp.features.user.product.ui.components.ProductCard
import com.example.shoestoreapp.features.user.product.ui.components.SearchBar
import com.example.shoestoreapp.features.user.product.ui.components.TopAppBar
import com.example.shoestoreapp.features.user.product.viewmodel.ProductListViewModel

/**
 * ProductListScreen: Composable screen hiển thị danh sách sản phẩm với đầy đủ UI
 * @param viewModel - ViewModel cung cấp dữ liệu và logic
 * @param onNavigateToDetail - Callback khi click vào sản phẩm (navigate to detail)
 * @param onTopMenuClick - Callback khi click menu ở TopAppBar
 * @param onNavigateToShoppingBag - Callback khi click shopping bag ở TopAppBar
 */
@Composable
fun ProductListScreen(
    viewModel: ProductListViewModel,
    onNavigateToDetail: (Int) -> Unit = {},
    onTopMenuClick: () -> Unit = {},
    onNavigateToShoppingBag: () -> Unit = {}
) {
    val productList = viewModel.productList.collectAsState(initial = emptyList())

    var selectedBottomTab by remember { mutableStateOf(BottomNavTab.HOME) }
    var selectedFilter by remember { mutableStateOf("All Shoes") }
    var searchText by remember { mutableStateOf("") }

    LaunchedEffect(searchText) {
        if (searchText.isNotEmpty()) {
            viewModel.onSearchChanged(searchText)
        }
    }

    LaunchedEffect(selectedFilter) {
        viewModel.onFilterSelected(selectedFilter)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                onMenuClick = onTopMenuClick,
                onShoppingBagClick = onNavigateToShoppingBag
            )
        },
        bottomBar = {
            BottomNavBar(
                selectedTab = selectedBottomTab,
                onTabSelected = { selectedBottomTab = it }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            SearchBar(
                searchText = searchText,
                onSearchChanged = { query ->
                    searchText = query
                }
            )

            Box(
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                FilterChips(
                    filters = listOf("All Shoes", "Air Max", "Dunk", "Pegasus", "Jordan"),
                    selectedFilter = selectedFilter,
                    onFilterSelected = { filter ->
                        selectedFilter = filter
                    }
                )
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(productList.value) { product ->
                    ProductCard(
                        product = product,
                        onProductClick = { productId ->
                            println("🟢 onNavigateToDetail called - productId: $productId")
                            onNavigateToDetail(productId)
                        },
                        onFavoriteClick = { productId ->
                            viewModel.toggleFavorite(productId)
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 800, widthDp = 360)
@Composable
fun ProductListScreenPreviewDefault() {
    ProductListScreen(
        viewModel = remember { ProductListViewModel() },
        onNavigateToDetail = { productId ->
            println("Navigating to product detail: $productId")
        },
        onTopMenuClick = {
            println("Menu clicked")
        },
        onNavigateToShoppingBag = {
            println("Shopping bag clicked")
        }
    )
}

