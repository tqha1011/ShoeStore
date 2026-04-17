package com.example.shoestoreapp.features.cart.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.shoestoreapp.features.cart.ui.components.CartItemCard
import com.example.shoestoreapp.features.cart.ui.components.CartSummarySection
import com.example.shoestoreapp.features.cart.ui.components.CartTopAppBar
import com.example.shoestoreapp.features.cart.ui.components.CheckoutButton
import com.example.shoestoreapp.features.cart.ui.components.EmptyCart
import com.example.shoestoreapp.features.cart.viewmodel.CartViewModel
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
 */
@Composable
fun CartScreen(
    viewModel: CartViewModel,
    onNavigateBack: () -> Unit = {},
    onNavigateToCheckout: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToShop: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    // Collect state từ ViewModel
    val cartItems = viewModel.cartItems.collectAsState(initial = emptyList())
    val cartSummary = viewModel.cartSummary.collectAsState()
    val isLoading = viewModel.isLoading.collectAsState()
    val errorMessage = viewModel.errorMessage.collectAsState()
    val isCartEmpty = viewModel.isCartEmpty.collectAsState()

    // Snackbar state
    val snackbarHostState = remember { SnackbarHostState() }

    // Hiển thị error message nếu có
    if (errorMessage.value.isNotEmpty()) {
        LaunchedEffect(errorMessage.value) {
            snackbarHostState.showSnackbar(
                message = errorMessage.value,
                duration = SnackbarDuration.Short
            )
            viewModel.clearErrorMessage()
        }
    }

    Scaffold(
        topBar = {
            CartTopAppBar(
                itemCount = cartItems.value.size,
                onBackClick = onNavigateBack
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        bottomBar = {
            BottomNavBar(
                selectedTab = BottomNavTab.BAG,
                onTabSelected = { tab ->
                    when (tab) {
                        BottomNavTab.HOME -> onNavigateToHome()
                        BottomNavTab.SHOP -> onNavigateToShop()
                        BottomNavTab.FAVORITES -> onNavigateToFavorites()
                        BottomNavTab.PROFILE -> onNavigateToProfile()
                        else -> {} // Already on BAG, no action needed
                    }
                }
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
            when {
                isLoading.value -> {
                    // Loading indicator
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                isCartEmpty.value -> {
                    // Empty cart UI
                    EmptyCart(
                        onContinueShopping = onNavigateBack
                    )
                }

                else -> {
                    // Cart items list with summary
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 8.dp)
                    ) {
                        // Cart items
                        items(cartItems.value) { item ->
                            CartItemCard(
                                item = item,
                                onIncreaseQuantity = { viewModel.onIncreaseQuantity(it) },
                                onDecreaseQuantity = { viewModel.onDecreaseQuantity(it) },
                                onRemove = { viewModel.onRemoveItem(it) },
                            )
                        }

                        // Cart summary
                        item {
                            CartSummarySection(
                                summary = cartSummary.value
                            )
                        }

                        // Checkout button
                        item {
                            CheckoutButton(
                                enabled = !isLoading.value && !isCartEmpty.value,
                                onClick = {
                                    if (viewModel.onCheckout()) {
                                        onNavigateToCheckout()
                                    }
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

