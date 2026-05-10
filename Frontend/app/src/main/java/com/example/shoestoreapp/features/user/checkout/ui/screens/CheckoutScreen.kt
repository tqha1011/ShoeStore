
package com.example.shoestoreapp.features.user.checkout.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shoestoreapp.features.user.checkout.data.remote.CheckOutRequestDto
import com.example.shoestoreapp.features.user.checkout.ui.components.CheckoutTopAppBar
import com.example.shoestoreapp.features.user.checkout.ui.components.CheckoutHeader
import com.example.shoestoreapp.features.user.checkout.ui.components.CurrencySwitcher
import com.example.shoestoreapp.features.user.checkout.ui.components.CompletePurchaseButton
import com.example.shoestoreapp.features.user.checkout.ui.components.DeliveryAddressSection
import com.example.shoestoreapp.features.user.checkout.ui.components.OrderSummarySection
import com.example.shoestoreapp.features.user.checkout.ui.components.PaymentMethodSection
import com.example.shoestoreapp.features.user.checkout.ui.components.PromoCodeSection
import com.example.shoestoreapp.features.user.checkout.ui.components.TermsDisclaimerText
import com.example.shoestoreapp.features.user.checkout.viewmodel.CheckoutViewModel
import com.example.shoestoreapp.features.user.checkout.viewmodel.PlaceOrderUiState




/**
 * CheckoutScreen: Màn hình Checkout chính.
 *
 * Architecture:
 * - MVVM Pattern: Sử dụng CheckoutViewModel để quản lý state
 * - StateFlow: Tất cả state được observe từ ViewModel
 * - Composable functions: UI được chia thành các component riêng biệt
 *
 * Cấu trúc:
 * 1. TopAppBar: Nike logo + Menu + Shopping Bag
 * 2. Content ScrollView:
 *    - Checkout Header
 *    - Currency Switcher
 *    - Delivery Address Section
 *    - Payment Method Section
 *    - Promo Code Section
 *    - Order Summary Section
 * 3. Bottom Actions:
 *    - Complete Purchase Button
 *    - Terms Disclaimer
 *
 * @param cartItems - Danh sách sản phẩm trong giỏ hàng để chuẩn bị checkout
 * @param onBackClick - Callback khi click back
 * @param onShoppingBagClick - Callback khi click shopping bag
 * @param onEditAddressClick - Callback khi click Edit address
 * @param onCompletePurchaseClick - Callback khi hoàn thành đơn hàng
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    cartItems: List<CheckOutRequestDto> = emptyList(),
    checkoutViewModel: CheckoutViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onShoppingBagClick: () -> Unit = {},
    onEditAddressClick: () -> Unit = {},
    onCompletePurchaseClick: () -> Unit = {}
) {

    // Observe state từ ViewModel
    val selectedCurrency = checkoutViewModel.selectedCurrency.collectAsState()
    val deliveryAddress = checkoutViewModel.deliveryAddress.collectAsState()
    val paymentMethod = checkoutViewModel.paymentMethod.collectAsState()
    val availablePaymentMethods = checkoutViewModel.availablePaymentMethods.collectAsState()
    val orderSummary = checkoutViewModel.orderSummary.collectAsState()
    val isLoading by checkoutViewModel.isLoading.collectAsState(initial = false)
    val errorMessage by checkoutViewModel.errorMessage.collectAsState()
    val cartItems = checkoutViewModel.cartItems.collectAsState()
    val placeOrderState by checkoutViewModel.placeOrderState.collectAsState()

    val context = LocalContext.current
    LaunchedEffect(placeOrderState) {
        when (placeOrderState) {
            is PlaceOrderUiState.Success -> {
                // Thành công -> Gọi hàm chuyển trang
                onCompletePurchaseClick()
            }
            is PlaceOrderUiState.Error -> {
                // Lỗi -> Báo Toast đỏ cho user biết
                val errorMsg = (placeOrderState as PlaceOrderUiState.Error).message
                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
            }
            else -> { /* Trạng thái Idle hoặc Loading thì cứ để nó chạy tự nhiên */ }
        }
    }
    // Nếu đang tải dữ liệu thì hiển thị loading
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

    // Xử lý lúc gọi API Prepare bị lỗi
    if (errorMessage.isNotEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Ups! Something went wrong while preparing your checkout.",
                    color = Color.Gray
                )
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
        return
    }
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        topBar = {
            CheckoutTopAppBar(
                onBackClick = onBackClick,
                onShoppingBagClick = onShoppingBagClick
            )
        },
        containerColor = Color.White,
        contentColor = Color.Black
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            // Main content area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Checkout Header
                CheckoutHeader()

                // Currency Switcher
                CurrencySwitcher(
                    selectedCurrency = selectedCurrency.value,
                    onCurrencySelected = { currency ->
                        checkoutViewModel.selectCurrency(currency)
                    }
                )

                // Delivery Address Section
                DeliveryAddressSection(
                    address = deliveryAddress.value,
                    onEditClick = onEditAddressClick
                )

                // Payment Method Section
                PaymentMethodSection(
                    selectedPaymentMethod = paymentMethod.value,
                    availablePaymentMethods = availablePaymentMethods.value,
                    onPaymentMethodSelected = { method ->
                        checkoutViewModel.selectPaymentMethod(method)
                    }
                )

                // Promo Code Section
                PromoCodeSection(
                    onApplyPromoCode = {
                        // Chưa thêm API Voucher
                    }
                )

                // Order Summary Section
                OrderSummarySection(
                    orderSummary = orderSummary.value,
                    selectedCurrency = selectedCurrency.value,
                    cartItems = cartItems.value
                )

                // Complete Purchase Button
                CompletePurchaseButton(
                    onCompletePurchaseClick = {
                        // Gọi hàm placeOrder mới chốt, lấy thông tin từ deliveryAddress truyền vào
                        checkoutViewModel.placeOrder(
                            fullName = "Phan Cao Minh Hieu", // Tạm để hardcode
                            address = "123 Innovation Drive, Silicon Valley", // Truyền địa chỉ đang chọn
                            phoneNumber = "0123456789",
                            fromUserCart = true
                        )
                    },
                    // Nếu đang gọi API đặt hàng thì xoay loading trên nút
                    isLoading = placeOrderState is PlaceOrderUiState.Loading,
                    modifier = Modifier.padding(top = 8.dp)
                )

                // Terms Disclaimer
                TermsDisclaimerText()
            }
        }
    }
}


