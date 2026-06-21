package com.example.shoestoreapp.features.user.cart.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.shoestoreapp.features.user.cart.ui.components.CartItemCard
import com.example.shoestoreapp.features.user.cart.ui.components.CartSummarySection
import com.example.shoestoreapp.features.user.cart.ui.components.CartTopAppBar
import com.example.shoestoreapp.features.user.cart.ui.components.CheckoutButton
import com.example.shoestoreapp.features.user.cart.ui.components.EmptyCart
import com.example.shoestoreapp.features.user.cart.viewmodel.CartUiState
import com.example.shoestoreapp.features.user.cart.viewmodel.CartViewModel
import com.example.shoestoreapp.features.user.product.ui.components.BottomNavBar
import com.example.shoestoreapp.features.user.product.ui.components.BottomNavTab

/**
 * CartScreen: Main Composable screen cho giỏ hàng
 *
 * Chức năng:
 * - Hiển thị danh sách items trong giỏ
 * - Hiển thị cart summary (subtotal, shipping, tax, total)
 * - Cho phép user:
 *   + Tăng/giảm số lượng item
 *   + Xóa item khỏi giỏ
 *   + Thêm item vào wishlist
 *   + Tiến hành checkout
 *
 * @param viewModel - CartViewModel cung cấp dữ liệu và logic
 * @param onNavigateBack - Callback quay lại màn hình trước
 * @param onNavigateToCheckout - Callback chuyển sang checkout screen
 * @param onContinueShoppingClick - Callback chuyển sang màn hình danh sách sản phẩm khi giỏ hàng trống
 */
@Composable
fun CartScreen(
    viewModel: CartViewModel,
    onNavigateBack: () -> Unit = {},
    onNavigateToCheckout: () -> Unit = {},
    onContinueShoppingClick: () -> Unit = {},
    onTabSelected: (BottomNavTab) -> Unit = {}
) {
    // Collect state từ ViewModel
    val cartUiState = viewModel.cartUiState.collectAsState()
    val updatingItemIds = viewModel.updatingItemIds.collectAsState()

    val itemCount = when (val state = cartUiState.value) {
        is CartUiState.Success -> state.items.size
        else -> 0
    }

    Scaffold(
        topBar = {
            CartTopAppBar(
                itemCount = itemCount,
                onBackClick = onNavigateBack
            )
        },
        bottomBar = {
            BottomNavBar(
                selectedTab = BottomNavTab.BAG,
                onTabSelected = onTabSelected
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9F9FF))
                .padding(paddingValues)
        ) {
            when (val state = cartUiState.value) {
                CartUiState.Loading -> {
                    // Loading indicator
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is CartUiState.Error -> {
                    Box(modifier = Modifier.align(Alignment.Center)) {
                        Button(onClick = viewModel::fetchCartItems) {
                            Text(text = state.message)
                        }
                    }
                }

                is CartUiState.Success -> {
                    if (state.isEmpty) {
                        EmptyCart(onContinueShopping = onContinueShoppingClick)
                    } else {
                        // Cart items list with summary
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 8.dp)
                        ) {
                            // Cart items
                            items(state.items, key = { it.cartItemId }) { item ->
                                CartItemCard(
                                    item = item,
                                    onIncreaseQuantity = { viewModel.onIncreaseQuantity(it) },
                                    onDecreaseQuantity = { viewModel.onDecreaseQuantity(it) },
                                    onRemove = { viewModel.onRemoveItem(it) },
                                    isUpdating = updatingItemIds.value.contains(item.cartItemId)
                                )
                            }

                            // Cart summary
                            item {
                                CartSummarySection(
                                    summary = state.summary
                                )
                            }

                            // Checkout button
                            item {
                                CheckoutButton(
                                    enabled = state.items.isNotEmpty(),
                                    onClick = {
                                        viewModel.onCheckout()
                                        // Chuyển sang màn hình Thanh toán
                                        onNavigateToCheckout()
                                    }
                                )
                            }

                            // Bottom spacing
                            item {
                                Box(modifier = Modifier.padding(bottom = 32.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
