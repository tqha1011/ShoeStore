package com.example.shoestoreapp.features.user.checkout.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    navController: NavController,
    checkoutViewModel: CheckoutViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onShoppingBagClick: () -> Unit = {},
    onEditAddressClick: () -> Unit = {},
    onNavigateToVoucherScreen: (Double) -> Unit = {},
    onNavigateToQRScreen: (InvoiceDto) -> Unit = {},
    onCompletePurchaseClick: () -> Unit = {}
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

    var receiverName by remember { mutableStateOf("") }
    var receiverPhone by remember { mutableStateOf("") }

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
        when (placeOrderState) {
            is PlaceOrderUiState.Success -> {
                val invoice = (placeOrderState as PlaceOrderUiState.Success).invoice
                if (paymentMethod.value.type == PaymentType.SePay) {
                    onNavigateToQRScreen(invoice)
                } else {
                    onCompletePurchaseClick()
                }
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
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = Color(0xFFFEF2F2)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "System Error",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "SOMETHING WENT WRONG",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Black,
                    letterSpacing = 0.5.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "We encountered a technical issue while preparing your checkout session. Please check your connection and try again.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Surface(
                    color = Color(0xFFF9FAFB),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Error Log: $errorMessage",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280),
                        modifier = Modifier.padding(14.dp),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = { checkoutViewModel.prepareCheckoutSession() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "TRY AGAIN",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    CheckoutHeader()

                    DeliveryAddressSection(
                        address = deliveryAddress.value,
                        onEditClick = onEditAddressClick
                    )

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
                            onValueChange = { receiverName = it },
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
                            onValueChange = { receiverPhone = it },
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
                            onNavigateToVoucherScreen(currentCartTotal)
                        }
                    )

                    OrderSummarySection(
                        orderSummary = orderSummary.value,
                        cartItems = cartItemsList.value
                    )

                    CompletePurchaseButton(
                        onCompletePurchaseClick = {
                            if (deliveryAddress.value.fullAddress.isBlank()) {
                                checkoutViewModel.showAddressWarning()
                                return@CompletePurchaseButton
                            }

                            if (checkoutViewModel.validateReceiverInfo(receiverName, receiverPhone)) {
                                checkoutViewModel.placeOrder(
                                    fullName = receiverName,
                                    address = deliveryAddress.value.fullAddress,
                                    phoneNumber = receiverPhone,
                                    fromUserCart = true
                                )
                            }
                        },
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
