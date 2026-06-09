package com.example.shoestoreapp.features.user.checkout.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.shoestoreapp.features.user.checkout.data.models.PaymentType
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
import com.example.shoestoreapp.features.user.product.ui.components.TopBanner
import com.example.shoestoreapp.features.user.voucher.data.models.VoucherUiModel


data class CheckoutScreenActions(
    val onBackClick: () -> Unit = {},
    val onShoppingBagClick: () -> Unit = {},
    val onEditAddressClick: () -> Unit = {},
    val onNavigateToVoucherScreen: (Double) -> Unit = {},
    val onNavigateToQRScreen: (InvoiceDto) -> Unit = {},
    val onCompletePurchaseClick: () -> Unit = {}
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    navController: NavController,
    checkoutViewModel: CheckoutViewModel = viewModel(),
    actions: CheckoutScreenActions = CheckoutScreenActions()
) {
    val deliveryAddress = checkoutViewModel.deliveryAddress.collectAsState()
    val paymentMethod = checkoutViewModel.paymentMethod.collectAsState()
    val availablePaymentMethods = checkoutViewModel.availablePaymentMethods.collectAsState()
    val orderSummary = checkoutViewModel.orderSummary.collectAsState()
    val isLoading by checkoutViewModel.isLoading.collectAsState(initial = false)
    val errorMessage by checkoutViewModel.errorMessage.collectAsState()
    val cartItemsList = checkoutViewModel.cartItems.collectAsState()
    val placeOrderState by checkoutViewModel.placeOrderState.collectAsState()
    val bannerMessage by checkoutViewModel.bannerMessage.collectAsState()
    val isBannerSuccess by checkoutViewModel.isBannerSuccess.collectAsState()
    val showBanner by checkoutViewModel.showBanner.collectAsState()

    val selectedProductVoucher by checkoutViewModel.selectedProductVoucher.collectAsState()
    val selectedShippingVoucher by checkoutViewModel.selectedShippingVoucher.collectAsState()

    var receiverName by rememberSaveable { mutableStateOf("") }
    var receiverPhone by rememberSaveable { mutableStateOf("") }

    ObserveCheckoutSideEffects(
        navController = navController,
        checkoutViewModel = checkoutViewModel,
        paymentType = paymentMethod.value.type,
        placeOrderState = placeOrderState,
        actions = actions
    )

    if (isLoading) {
        CheckoutLoadingScreen()
        return
    }

    if (errorMessage.isNotEmpty()) {
        CheckoutErrorScreen(
            errorMessage = errorMessage,
            onTryAgain = { checkoutViewModel.prepareCheckoutSession() }
        )
        return
    }

    val handleCompletePurchase = {
        if (deliveryAddress.value.fullAddress.isBlank()) {
            checkoutViewModel.showAddressWarning()
        } else if (checkoutViewModel.validateReceiverInfo(receiverName, receiverPhone)) {
            checkoutViewModel.placeOrder(
                fullName = receiverName,
                address = deliveryAddress.value.fullAddress,
                phoneNumber = receiverPhone,
                fromUserCart = true
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            topBar = {
                CheckoutTopAppBar(
                    onBackClick = actions.onBackClick,
                    onShoppingBagClick = actions.onShoppingBagClick
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    CheckoutHeader()

                    DeliveryAddressSection(
                        address = deliveryAddress.value,
                        onEditClick = actions.onEditAddressClick
                    )

                    ReceiverInformationSection(
                        receiverName = receiverName,
                        receiverPhone = receiverPhone,
                        onNameChange = { receiverName = it },
                        onPhoneChange = { receiverPhone = it }
                    )

                    PaymentMethodSection(
                        selectedPaymentMethod = paymentMethod.value,
                        availablePaymentMethods = availablePaymentMethods.value,
                        onPaymentMethodSelected = { method ->
                            checkoutViewModel.selectPaymentMethod(method)
                        }
                    )

                    CheckoutVoucherRow(
                        selectedProductVoucher = selectedProductVoucher,
                        selectedShippingVoucher = selectedShippingVoucher,
                        onClick = {
                            val currentCartTotal = orderSummary.value.subtotal
                            actions.onNavigateToVoucherScreen(currentCartTotal)
                        },
                        onClearClick = {
                            checkoutViewModel.clearAppliedVouchers()
                        }
                    )

                    OrderSummarySection(
                        orderSummary = orderSummary.value,
                        cartItems = cartItemsList.value
                    )

                    CompletePurchaseButton(
                        onCompletePurchaseClick = handleCompletePurchase,
                        isLoading = placeOrderState is PlaceOrderUiState.Loading,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    TermsDisclaimerText()
                }
            }
        }

        Box(modifier = Modifier.align(Alignment.TopCenter)) {
            TopBanner(
                message = bannerMessage,
                isSuccess = isBannerSuccess,
                isVisible = showBanner,
                onDismiss = { checkoutViewModel.hideBanner() }
            )
        }
    }
}

// --- PRIVATE COMPOSABLES ---

@Composable
private fun ObserveCheckoutSideEffects(
    navController: NavController,
    checkoutViewModel: CheckoutViewModel,
    paymentType: PaymentType,
    placeOrderState: PlaceOrderUiState,
    actions: CheckoutScreenActions
) {
    val navBackStackEntry = navController.currentBackStackEntry
    val returnedVoucherJson = navBackStackEntry?.savedStateHandle?.get<String>("selected_voucher_json")

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val selectedAddressId by savedStateHandle?.getStateFlow<String?>("selected_address_id", null)?.collectAsState(initial = null) ?: remember { mutableStateOf(null) }

    LaunchedEffect(selectedAddressId) {
        selectedAddressId?.let { id ->
            checkoutViewModel.onAddressSelected(id)
            savedStateHandle?.remove<String>("selected_address_id")
        }
    }

    LaunchedEffect(returnedVoucherJson) {
        returnedVoucherJson?.let { jsonString ->
            val voucher = com.google.gson.Gson().fromJson(jsonString, VoucherUiModel::class.java)
            checkoutViewModel.applyVoucher(voucher)
            navBackStackEntry.savedStateHandle.remove<String>("selected_voucher_json")
        }
    }

    LaunchedEffect(placeOrderState) {
        if (placeOrderState is PlaceOrderUiState.Success) {
            val invoice = placeOrderState.invoice
            if (paymentType == PaymentType.SePay) {
                actions.onNavigateToQRScreen(invoice)
            } else {
                actions.onCompletePurchaseClick()
            }
        }
    }
}

@Composable
private fun CheckoutLoadingScreen() {
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
}

@Composable
private fun ReceiverInformationSection(
    receiverName: String,
    receiverPhone: String,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "RECEIVER INFORMATION",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )

        OutlinedTextField(
            value = receiverName,
            onValueChange = onNameChange,
            label = { Text("Full Name") },
            placeholder = { Text("Enter receiver's name") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color(0xFFE5E7EB),
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.Gray
            ),
            singleLine = true
        )

        OutlinedTextField(
            value = receiverPhone,
            onValueChange = onPhoneChange,
            label = { Text("Phone Number") },
            placeholder = { Text("Enter phone number") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color(0xFFE5E7EB),
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.Gray
            ),
            singleLine = true
        )
    }
}