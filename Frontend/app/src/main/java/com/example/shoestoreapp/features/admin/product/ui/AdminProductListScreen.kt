package com.example.shoestoreapp.features.admin.product.ui
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.shoestoreapp.features.admin.product.ui.components.AdminBottomNavBar
import com.example.shoestoreapp.features.admin.product.ui.components.AdminBottomNavTab
import com.example.shoestoreapp.features.admin.product.ui.components.AdminFilterChips
import com.example.shoestoreapp.features.admin.product.ui.components.AdminProductCard
import com.example.shoestoreapp.features.admin.product.ui.components.AdminSearchBar
import com.example.shoestoreapp.features.admin.product.ui.components.AdminTopAppBar
import com.example.shoestoreapp.features.admin.product.viewmodel.AdminProductListViewModel
/**
 * Màn hình quản lý sản phẩm cho Admin.
 * 
 * Cấu trúc:
 * 1. TopAppBar: Menu, NIKE text, Add Product button
 * 2. SearchBar: Tìm kiếm sản phẩm
 * 3. FilterChips: Lọc theo status (ALL, IN STOCK, LOW STOCK, OUT OF STOCK)
 * 4. Product Grid: Hiển thị danh sách sản phẩm (2 cột)
 * 5. BottomNavBar: Navigation giữa các trang admin
 * 
 * @param viewModel - ViewModel quản lý state
 * @param onMenuClick - Callback khi click menu
 * @param onAddProductClick - Callback khi click Add Product
 * @param onTabSelected - Callback khi click tab ở bottom nav
 */
@Composable
fun AdminProductListScreen(
    viewModel: AdminProductListViewModel = AdminProductListViewModel(),
    onMenuClick: () -> Unit = {},
    onAddProductClick: () -> Unit = {},
    onTabSelected: (AdminBottomNavTab) -> Unit = {}
) {
    val products by viewModel.products.collectAsState(initial = emptyList())
    val selectedFilter by viewModel.selectedFilter.collectAsState(initial = "ALL PRODUCTS")
    val searchText by viewModel.searchText.collectAsState(initial = "")
    val isLoading by viewModel.isLoading.collectAsState(initial = false)
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.width(50.dp),
                color = Color.Black
            )
        }
        return
    }
    Scaffold(
        topBar = {
            AdminTopAppBar(
                onMenuClick = onMenuClick,
                onAddProductClick = onAddProductClick
            )
        },
        bottomBar = {
            AdminBottomNavBar(
                selectedTab = AdminBottomNavTab.ADMIN,
                onTabSelected = onTabSelected
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            // Search Bar
            AdminSearchBar(
                searchText = searchText,
                onSearchChanged = { query ->
                    viewModel.onSearchChanged(query)
                }
            )
            // Filter Chips
            AdminFilterChips(
                selectedFilter = selectedFilter,
                onFilterSelected = { filter ->
                    viewModel.onFilterChanged(filter)
                }
            )
            // Product Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
            ) {
                items(products.size) { index ->
                    AdminProductCard(
                        product = products[index],
                        onProductClick = { _ ->
                            println("Edit product: ")
                        }
                    )
                }
            }
        }
    }
}
