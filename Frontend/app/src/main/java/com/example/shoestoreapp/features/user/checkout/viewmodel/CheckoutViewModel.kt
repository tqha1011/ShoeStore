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
import org.json.JSONObject
import retrofit2.HttpException

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

    fun prepareCheckoutSession() {
        val itemsToCheckout = CheckoutSession.pendingItems
        if (itemsToCheckout.isEmpty()) return

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
                    _errorMessage.value = ""
                    _isLoading.value = false
                }.onFailure { exception ->
                    _isLoading.value = false

                    // LỖI ÁP VOUCHER
                    if (exception is HttpException && exception.code() == 400) {
                        val parsedMessage = parseErrorMessage(exception)
                        _placeOrderState.value = PlaceOrderUiState.Error(parsedMessage)

                        _selectedProductVoucher.value = null
                        _selectedShippingVoucher.value = null
                        prepareCheckoutSession()

                    } else {
                        _errorMessage.value = exception.message ?: "Unknown error during checkout preparation"
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
        prepareCheckoutSession()
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

                    // LỖI KHI BẤM THANH TOÁN
                    if (exception is HttpException && exception.code() == 400) {
                        val parsedMessage = parseErrorMessage(exception)
                        // Bắn lỗi vào State để hiện Toast
                        _placeOrderState.value = PlaceOrderUiState.Error(parsedMessage)
                    } else {
                        // Lỗi khác
                        _placeOrderState.value = PlaceOrderUiState.Error(exception.message ?: "Unknown error during placing order")
                    }
                }
            } catch (e: Exception) {
                _placeOrderState.value = PlaceOrderUiState.Error(e.message ?: "Unknown error during placing order")
            }
        }
    }

    // Hàm phụ trợ dùng chung để bóc tách JSON lỗi từ Backend
    private fun parseErrorMessage(exception: HttpException): String {
        val errorString = exception.response()?.errorBody()?.string() ?: ""
        var parsedMessage = "Dữ liệu không hợp lệ"
        try {
            val jsonObject = JSONObject(errorString)
            parsedMessage = jsonObject.getString("message")
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return parsedMessage
    }
}