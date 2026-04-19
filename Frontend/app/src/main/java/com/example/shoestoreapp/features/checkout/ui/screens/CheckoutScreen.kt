package com.example.shoestoreapp.features.checkout.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shoestoreapp.features.checkout.ui.components.CheckoutTopAppBar
import com.example.shoestoreapp.features.checkout.ui.components.CheckoutHeader
import com.example.shoestoreapp.features.checkout.ui.components.CurrencySwitcher
import com.example.shoestoreapp.features.checkout.ui.components.CompletePurchaseButton
import com.example.shoestoreapp.features.checkout.ui.components.DeliveryAddressSection
import com.example.shoestoreapp.features.checkout.ui.components.OrderSummarySection
import com.example.shoestoreapp.features.checkout.ui.components.PaymentMethodSection
import com.example.shoestoreapp.features.checkout.ui.components.PromoCodeSection
import com.example.shoestoreapp.features.checkout.ui.components.TermsDisclaimerText
import com.example.shoestoreapp.features.checkout.viewmodel.CheckoutViewModel

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
 * @param onBackClick - Callback khi click back
 * @param onShoppingBagClick - Callback khi click shopping bag
 * @param onEditAddressClick - Callback khi click Edit address
 * @param onCompletePurchaseClick - Callback khi hoàn thành đơn hàng
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
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
    val isLoading = checkoutViewModel.isLoading.collectAsState()

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
                    onApplyPromoCode = { promoCode ->
                        checkoutViewModel.applyPromoCode(promoCode)
                    }
                )

                // Order Summary Section
                OrderSummarySection(
                    orderSummary = orderSummary.value,
                    selectedCurrency = selectedCurrency.value
                )

                // Complete Purchase Button
                CompletePurchaseButton(
                    onCompletePurchaseClick = {
                        checkoutViewModel.completePurchase(
                            onSuccess = { onCompletePurchaseClick() },
                            onError = { /* Handle error */ }
                        )
                    },
                    isLoading = isLoading.value,
                    modifier = Modifier.padding(top = 8.dp)
                )

                // Terms Disclaimer
                TermsDisclaimerText()
            }
        }
    }
}


