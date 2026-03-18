package com.example.shoestoreapp.features.product.ui.product_list

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
import com.example.shoestoreapp.features.product.ui.components.BottomNavBar
import com.example.shoestoreapp.features.product.ui.components.BottomNavTab
import com.example.shoestoreapp.features.product.ui.components.FilterChips
import com.example.shoestoreapp.features.product.ui.components.ProductCard
import com.example.shoestoreapp.features.product.ui.components.SearchBar
import com.example.shoestoreapp.features.product.ui.components.TopAppBar
import com.example.shoestoreapp.features.product.viewmodel.ProductListViewModel

/**
 * ProductListScreen: Composable screen hiển thị danh sách sản phẩm với đầy đủ UI
 * @param viewModel - ViewModel cung cấp dữ liệu và logic
 * @param onNavigateToDetail - Callback khi click vào sản phẩm (navigate to detail)
 * @param onTopMenuClick - Callback khi click menu ở TopAppBar
 * @param onNavigateToShoppingBag - Callback khi click shopping bag ở TopAppBar (navigate to shopping bag)
 */
@Composable
fun ProductListScreen(
    viewModel: ProductListViewModel,
    onNavigateToDetail: (Int) -> Unit = {},
    onTopMenuClick: () -> Unit = {},
    onNavigateToShoppingBag: () -> Unit = {}
) {
    // ============ COLLECT STATE TỪ VIEWMODEL ============
    val productList = viewModel.productList.collectAsState(initial = emptyList())

    // ============ LOCAL STATE ============
    var selectedBottomTab by remember { mutableStateOf(BottomNavTab.HOME) }
    var selectedFilter by remember { mutableStateOf("All Shoes") }
    var searchText by remember { mutableStateOf("") }

    // ============ SEARCH LOGIC ============
    /**
     * LaunchedEffect: Chạy side effect khi searchText thay đổi
     */
    LaunchedEffect(searchText) {
        if (searchText.isNotEmpty()) {
            viewModel.searchProducts(searchText)
        }
    }

    // ============ FILTER LOGIC ============
    /**
     * LaunchedEffect: Chạy side effect khi selectedFilter thay đổi
     * - Được gọi lần đầu khi component được tạo
     * - Được gọi lại mỗi khi selectedFilter thay đổi
     * - Được cancel khi component bị destroy
     */
    LaunchedEffect(selectedFilter) {
        viewModel.filterProducts(selectedFilter)
    }

    // ============ MAIN SCAFFOLD ============
    /**
     * Scaffold giúp quản lý layout toàn màn hình với:
     * - topBar: AppBar phía trên
     * - bottomBar: Navigation bar phía dưới
     * - content: Nội dung chính ở giữa
     */
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
        // ============ MAIN CONTENT COLUMN ============
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            // ========== SEARCH BAR ==========
            SearchBar(
                searchText = searchText,         // ← Nhận state từ parent
                onSearchChanged = { query ->
                    searchText = query          // ← Update state ở parent
                }
            )

            // ========== FILTER CHIPS ==========
            Box(
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                FilterChips(
                    filters = listOf("All Shoes", "Air Max", "Dunk", "Pegasus", "Jordan"),
                    selectedFilter = selectedFilter,  // ← Nhận state từ parent
                    onFilterSelected = { filter ->
                        selectedFilter = filter      // ← Update state ở parent
                    }
                )
            }

            // ========== PRODUCT GRID ==========
            /**
             * LazyVerticalGrid:
             * - Composable để hiển thị list theo grid layout
             * - GridCells.Fixed(2) = 2 cột cố định
             * - Lazy = chỉ render các item visible trên màn hình (optimize performance)
             */
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
                        // Click vào card -> Navigate đến trang chi tiết
                        // Truyền productId vào callback để điều hướng
                        onProductClick = { onNavigateToDetail(product.id) },
                        // Click -> Toggle favorite
                        onFavoriteClick = { productId ->
                            viewModel.toggleFavorite(productId)
                        }
                    )
                }
            }
        }
    }
}


/**
 * ProductListScreenPreview: Preview của ProductListScreen
 *
 * Cách xem:
 * - Nhấn "Preview" ở bên phải Android Studio
 * - Hoặc Ctrl+Shift+P
 * - Sẽ hiển thị giao diện như trên điện thoại thực tế
 */
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
