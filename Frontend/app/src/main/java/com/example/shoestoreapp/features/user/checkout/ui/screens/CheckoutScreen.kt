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
import androidx.navigation.NavController
import com.example.shoestoreapp.features.user.checkout.data.models.PaymentType
import com.example.shoestoreapp.features.user.checkout.data.remote.CheckOutRequestDto
import com.example.shoestoreapp.features.user.checkout.data.remote.InvoiceDto
import com.example.shoestoreapp.features.user.checkout.ui.components.CheckoutTopAppBar
import com.example.shoestoreapp.features.user.checkout.ui.components.CheckoutHeader
import com.example.shoestoreapp.features.user.checkout.ui.components.CheckoutVoucherRow
import com.example.shoestoreapp.features.user.checkout.ui.components.CompletePurchaseButton
import com.example.shoestoreapp.features.user.checkout.ui.components.DeliveryAddressSection
import com.example.shoestoreapp.features.user.checkout.ui.components.OrderSummarySection
import com.example.shoestoreapp.features.user.checkout.ui.components.PaymentMethodSection
import com.example.shoestoreapp.features.user.checkout.ui.components.TermsDisclaimerText
import com.example.shoestoreapp.features.user.checkout.viewmodel.CheckoutViewModel
import com.example.shoestoreapp.features.user.checkout.viewmodel.PlaceOrderUiState
import com.example.shoestoreapp.features.user.voucher.data.models.VoucherUiModel

/**
 * CheckoutScreen: Main Checkout Screen.
 *
 * Architecture:
 * - MVVM Pattern: Uses CheckoutViewModel to manage state
 * - StateFlow: All states are observed from ViewModel
 * - Composable functions: UI is divided into separate components
 *
 * Structure:
 * 1. TopAppBar: Nike logo + Menu + Shopping Bag
 * 2. Content ScrollView:
 * - Checkout Header
 * - Delivery Address Section
 * - Payment Method Section
 * - Checkout Voucher Row (Replaced PromoCodeSection)
 * - Order Summary Section
 * 3. Bottom Actions:
 * - Complete Purchase Button
 * - Terms Disclaimer
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    navController: NavController,
    checkoutViewModel: CheckoutViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onShoppingBagClick: () -> Unit = {},
    onEditAddressClick: () -> Unit = {},
    onNavigateToVoucherScreen: () -> Unit = {},
    onNavigateToQRScreen: (InvoiceDto) -> Unit = {},
    onCompletePurchaseClick: () -> Unit = {}
) {

    // Observe state from ViewModel
    val deliveryAddress = checkoutViewModel.deliveryAddress.collectAsState()
    val paymentMethod = checkoutViewModel.paymentMethod.collectAsState()
    val availablePaymentMethods = checkoutViewModel.availablePaymentMethods.collectAsState()
    val orderSummary = checkoutViewModel.orderSummary.collectAsState()
    val isLoading by checkoutViewModel.isLoading.collectAsState(initial = false)
    val errorMessage by checkoutViewModel.errorMessage.collectAsState()
    val cartItemsList = checkoutViewModel.cartItems.collectAsState()
    val placeOrderState by checkoutViewModel.placeOrderState.collectAsState()

    // Observe Voucher states
    val selectedProductVoucher by checkoutViewModel.selectedProductVoucher.collectAsState()
    val selectedShippingVoucher by checkoutViewModel.selectedShippingVoucher.collectAsState()

    val context = LocalContext.current

    val navBackStackEntry = navController.currentBackStackEntry
    val returnedVoucherJson = navBackStackEntry?.savedStateHandle?.get<String>("selected_voucher_json")

    LaunchedEffect(returnedVoucherJson) {
        returnedVoucherJson?.let { jsonString ->
            // JSON -> OBJECT
            val voucher = com.google.gson.Gson().fromJson(jsonString, VoucherUiModel::class.java)
            // Goi ham ap dung voucher
            checkoutViewModel.applyVoucher(voucher)
            navBackStackEntry?.savedStateHandle?.remove<String>("selected_voucher_json")
        }
    }

    LaunchedEffect(placeOrderState) {
        when (placeOrderState) {
            is PlaceOrderUiState.Success -> {
                val invoice = (placeOrderState as PlaceOrderUiState.Success).invoice

                // Kiểm tra phương thức thanh toán hiện tại
                if (paymentMethod.value.type == PaymentType.SePay) {
                    // Nếu là SePay -> Qua màn hình QR
                    onNavigateToQRScreen(invoice)
                } else {
                    // Nếu là COD -> Qua màn hình thành công
                    onCompletePurchaseClick()
                }
            }
            is PlaceOrderUiState.Error -> {
                val errorMsg = (placeOrderState as PlaceOrderUiState.Error).message
                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
            }
            else -> { }
        }
    }

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
                    text = "Oops! Something went wrong while preparing your checkout.",
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

                // Voucher Section
                CheckoutVoucherRow(
                    selectedProductVoucher = selectedProductVoucher,
                    selectedShippingVoucher = selectedShippingVoucher,
                    onClick = onNavigateToVoucherScreen
                )

                // Order Summary Section
                OrderSummarySection(
                    orderSummary = orderSummary.value,
                    cartItems = cartItemsList.value
                )

                // Complete Purchase Button
                CompletePurchaseButton(
                    onCompletePurchaseClick = {
                        checkoutViewModel.placeOrder(
                            fullName = "Phan Cao Minh Hieu", // Hardcoded for now
                            address = "123 Innovation Drive, Silicon Valley",
                            phoneNumber = "0123456789",
                            fromUserCart = true
                        )
                    },
                    isLoading = placeOrderState is PlaceOrderUiState.Loading,
                    modifier = Modifier.padding(top = 8.dp)
                )

                // Terms Disclaimer
                TermsDisclaimerText()
            }
        }
    }
}