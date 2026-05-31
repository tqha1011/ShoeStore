package com.example.shoestoreapp.features.user.checkout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestoreapp.features.user.checkout.data.models.DeliveryAddress
import com.example.shoestoreapp.features.user.checkout.data.models.OrderSummary
import com.example.shoestoreapp.features.user.checkout.data.models.PaymentMethod
import com.example.shoestoreapp.features.user.checkout.data.models.PaymentType
import com.example.shoestoreapp.features.user.checkout.data.remote.CheckOutItemDto
import com.example.shoestoreapp.features.user.checkout.data.remote.InvoiceDto
import com.example.shoestoreapp.features.user.checkout.data.remote.toOrderSummary
import com.example.shoestoreapp.features.user.checkout.data.repositories.CheckOutRepository
import com.example.shoestoreapp.features.user.checkout.data.repositories.CheckoutRepositoryImpl
import com.example.shoestoreapp.features.user.checkout.data.repositories.CheckoutSession
import com.example.shoestoreapp.features.user.checkout.data.remote.PlaceOrderRequestDto
import com.example.shoestoreapp.features.user.checkout.data.remote.PrepareCheckOutRequestDto
import com.example.shoestoreapp.features.user.voucher.data.models.VoucherUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface PlaceOrderUiState {
    object Idle : PlaceOrderUiState
    data class Success(val invoice: InvoiceDto) : PlaceOrderUiState
    object Loading : PlaceOrderUiState
    data class Error(val message: String) : PlaceOrderUiState
}

class CheckoutViewModel(
    private val checkoutRepository: CheckOutRepository = CheckoutRepositoryImpl(),
) : ViewModel() {

    // ============ STATE MANAGEMENT ============

    private val _deliveryAddress = MutableStateFlow(DeliveryAddress())
    val deliveryAddress: StateFlow<DeliveryAddress> = _deliveryAddress.asStateFlow()

    private val _paymentMethod = MutableStateFlow(PaymentMethod())
    val paymentMethod: StateFlow<PaymentMethod> = _paymentMethod.asStateFlow()

    private val _orderSummary = MutableStateFlow(OrderSummary())
    val orderSummary: StateFlow<OrderSummary> = _orderSummary.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _placeOrderState = MutableStateFlow<PlaceOrderUiState>(PlaceOrderUiState.Idle)
    val placeOrderState = _placeOrderState.asStateFlow()

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage.asStateFlow()

    private val _cartItems = MutableStateFlow<List<CheckOutItemDto>>(emptyList())
    val cartItems: StateFlow<List<CheckOutItemDto>> = _cartItems.asStateFlow()

    // --- VOUCHER MANAGEMENT ---
    private val _selectedProductVoucher = MutableStateFlow<VoucherUiModel?>(null)
    val selectedProductVoucher = _selectedProductVoucher.asStateFlow()

    private val _selectedShippingVoucher = MutableStateFlow<VoucherUiModel?>(null)
    val selectedShippingVoucher = _selectedShippingVoucher.asStateFlow()

    private val _availablePaymentMethods = MutableStateFlow(
        listOf(
            PaymentMethod(id = 1, displayName = "SePay", expiryDate = "12/26", type = PaymentType.SePay, isDefault = true),
            PaymentMethod(id = 2, displayName = "COD (Cash on Delivery)", expiryDate = "12/26", type = PaymentType.COD, isDefault = false)
        )
    )
    val availablePaymentMethods: StateFlow<List<PaymentMethod>> = _availablePaymentMethods.asStateFlow()

    // ============ INITIALIZATION ============

    init {
        val itemsToCheckout = CheckoutSession.pendingItems
        if (itemsToCheckout.isNotEmpty()) {
            prepareCheckoutSession()
        }
    }

    // ============ PUBLIC FUNCTIONS ============

    /**
     * Tự động lấy danh sách hàng và Voucher (ID dạng Int) để gọi API tính tiền
     */
    fun prepareCheckoutSession() {
        val itemsToCheckout = CheckoutSession.pendingItems
        if (itemsToCheckout.isEmpty()) return

        // Nhặt numericId để gửi lên BE
        val activeVoucherIds = listOfNotNull(
            _selectedProductVoucher.value?.numericId,
            _selectedShippingVoucher.value?.numericId
        )

        val requestBody = PrepareCheckOutRequestDto(
            checkOutList = itemsToCheckout,
            voucherIds = activeVoucherIds
        )

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = checkoutRepository.prepareCheckOut(requestBody)
                result.onSuccess { response ->
                    _orderSummary.value = response.summary.toOrderSummary()
                    _cartItems.value = response.items
                    _isLoading.value = false
                }.onFailure { exception ->
                    _isLoading.value = false
                    val errorMsg = exception.message ?: "Unknown error during checkout preparation"
                    _errorMessage.value = errorMsg

                    // Nếu Backend từ chối Voucher, tự động gỡ ra và load lại bill gốc
                    if (errorMsg.contains("Voucher", ignoreCase = true)) {
                        _selectedProductVoucher.value = null
                        _selectedShippingVoucher.value = null
                        prepareCheckoutSession()
                    }
                }
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = e.message ?: "Unknown error during checkout preparation"
            }
        }
    }

    fun applyVoucher(voucher: VoucherUiModel) {
        if (voucher.scope.uppercase() == "SHIPPING") {
            _selectedShippingVoucher.value = voucher
        } else {
            _selectedProductVoucher.value = voucher
        }
        prepareCheckoutSession() // Yêu cầu Backend tính lại tiền
    }

    fun removeVoucher(isShippingVoucher: Boolean) {
        if (isShippingVoucher) {
            _selectedShippingVoucher.value = null
        } else {
            _selectedProductVoucher.value = null
        }
        prepareCheckoutSession() // Yêu cầu Backend tính lại tiền
    }

    fun selectPaymentMethod(paymentMethod: PaymentMethod) {
        _paymentMethod.value = paymentMethod
    }

    fun updateDeliveryAddress(address: DeliveryAddress) {
        _deliveryAddress.value = address
    }

    fun placeOrder(
        fullName: String,
        address: String,
        phoneNumber: String,
        fromUserCart: Boolean
    ) {
        viewModelScope.launch {
            _placeOrderState.value = PlaceOrderUiState.Loading
            val itemsToBuy = CheckoutSession.pendingItems
            val selectedPaymentId = _paymentMethod.value.id

            if (itemsToBuy.isEmpty()) {
                _placeOrderState.value = PlaceOrderUiState.Error("No items to place order")
                return@launch
            }

            // Đổi sang lấy numericId (Int)
            val activeVoucherIdsForPlaceOrder = listOfNotNull(
                _selectedProductVoucher.value?.numericId,
                _selectedShippingVoucher.value?.numericId
            )

            val requestDto = PlaceOrderRequestDto(
                items = itemsToBuy,
                voucherIds = activeVoucherIdsForPlaceOrder,
                fullName = fullName,
                address = address,
                paymentId = selectedPaymentId,
                phoneNumber = phoneNumber
            )

            try {
                val result = checkoutRepository.placeOrder(fromUserCart, requestDto)
                result.onSuccess { invoice ->
                    _placeOrderState.value = PlaceOrderUiState.Success(invoice)
                    CheckoutSession.pendingItems = emptyList()
                }.onFailure { exception ->
                    _placeOrderState.value = PlaceOrderUiState.Error(exception.message ?: "Unknown error during placing order")
                }
            } catch (e: Exception) {
                _placeOrderState.value = PlaceOrderUiState.Error(e.message ?: "Unknown error during placing order")
            }
        }
    }
}