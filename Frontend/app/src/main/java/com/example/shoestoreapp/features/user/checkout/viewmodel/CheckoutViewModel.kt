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
import com.example.shoestoreapp.features.user.profile.data.repositories.AddressRepository
import com.example.shoestoreapp.features.user.profile.data.repositories.AddressRepositoryImpl
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
    private val addressRepository: AddressRepository = AddressRepositoryImpl()
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

    // Banner properties
    private val _bannerMessage = MutableStateFlow("")
    val bannerMessage = _bannerMessage.asStateFlow()

    private val _isBannerSuccess = MutableStateFlow(true)
    val isBannerSuccess = _isBannerSuccess.asStateFlow()

    private val _showBanner = MutableStateFlow(false)
    val showBanner = _showBanner.asStateFlow()

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

    // ============ MANUAL PAYMENT STATUS ============

    // Quản lý trạng thái đang loading của nút kiểm tra thanh toán
    private val _isCheckingPayment = MutableStateFlow(false)
    val isCheckingPayment = _isCheckingPayment.asStateFlow()


    // ============ INITIALIZATION ============

    init {
        val itemsToCheckout = CheckoutSession.pendingItems
        if (itemsToCheckout.isNotEmpty()) {
            prepareCheckoutSession()
        }
        fetchDefaultAddress()
    }

    // ============ ADDRESS FUNCTIONS ============

    private fun fetchDefaultAddress() {
        viewModelScope.launch {
            addressRepository.getAllAddresses().onSuccess { addresses ->
                val default = addresses.find { it.isDefault } ?: addresses.firstOrNull()
                default?.let {
                    _deliveryAddress.value = _deliveryAddress.value.copy(
                        fullAddress = it.address
                    )
                }
            }.onFailure {
                _bannerMessage.value = "Failed to load default address"
                _isBannerSuccess.value = false
                _showBanner.value = true
            }
        }
    }

    fun onAddressSelected(addressId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            addressRepository.getAddressById(addressId)
                .onSuccess { addressDto ->
                    _deliveryAddress.value = _deliveryAddress.value.copy(
                        fullAddress = addressDto.address
                    )
                    _isLoading.value = false
                }
                .onFailure { _ ->
                    _isLoading.value = false
                    _bannerMessage.value = "Failed to load selected address"
                    _isBannerSuccess.value = false
                    _showBanner.value = true
                }
        }
    }

    // ============ PUBLIC FUNCTIONS ============

    fun hideBanner() {
        _showBanner.value = false
    }

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

            checkoutRepository.prepareCheckOut(requestBody)
                .onSuccess { response ->
                    _orderSummary.value = response.summary.toOrderSummary()
                    _cartItems.value = response.items
                    _errorMessage.value = ""
                    _isLoading.value = false
                }
                .onFailure { exception ->
                    _isLoading.value = false
                    val msg = exception.message ?: "Unknown error during checkout preparation"

                    // Xử lý lỗi áp voucher
                    if (msg.contains("voucher", ignoreCase = true) || msg.contains("invalid", ignoreCase = true)) {
                        _bannerMessage.value = msg
                        _isBannerSuccess.value = false
                        _showBanner.value = true
                        _placeOrderState.value = PlaceOrderUiState.Error(msg)

                        // Reset voucher và thử tính toán lại
                        _selectedProductVoucher.value = null
                        _selectedShippingVoucher.value = null
                        prepareCheckoutSession()
                    } else {
                        _errorMessage.value = msg
                        _bannerMessage.value = msg
                        _isBannerSuccess.value = false
                        _showBanner.value = true
                    }
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

    fun clearAppliedVouchers() {
        // Reset states to null
        _selectedProductVoucher.value = null
        _selectedShippingVoucher.value = null

        // Trigger API recalculation without vouchers
        prepareCheckoutSession()
    }

    fun selectPaymentMethod(paymentMethod: PaymentMethod) {
        _paymentMethod.value = paymentMethod
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
                val msg = "No items to place order"
                _bannerMessage.value = msg
                _isBannerSuccess.value = false
                _showBanner.value = true
                _placeOrderState.value = PlaceOrderUiState.Error(msg)
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

            checkoutRepository.placeOrder(fromUserCart, requestDto)
                .onSuccess { invoice ->
                    _placeOrderState.value = PlaceOrderUiState.Success(invoice)
                    CheckoutSession.pendingItems = emptyList()
                }
                .onFailure { exception ->
                    val msg = exception.message ?: "Unknown error during placing order"
                    _bannerMessage.value = msg
                    _isBannerSuccess.value = false
                    _showBanner.value = true
                    _placeOrderState.value = PlaceOrderUiState.Error(msg)
                }
        }
    }

    fun showAddressWarning() {
        _bannerMessage.value = "Please select a delivery address before completing purchase."
        _isBannerSuccess.value = false
        _showBanner.value = true
    }

    fun validateReceiverInfo(name: String, phone: String): Boolean {
        if (name.isBlank() || phone.isBlank()) {
            _bannerMessage.value = "Please enter receiver's name and phone number"
            _isBannerSuccess.value = false
            _showBanner.value = true
            return false
        }
        return true
    }

    // HÀM KIỂM TRA THANH TOÁN THỦ CÔNG QUA NÚT
    fun verifyPaymentManual(
        orderCode: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isCheckingPayment.value = true

            checkoutRepository.checkPaymentStatus(orderCode).fold(
                onSuccess = { data ->
                    if (data.status.equals("Paid", ignoreCase = true) && data.remainingAmount == 0.0) {
                        onSuccess()
                    } else {
                        _bannerMessage.value = "Hệ thống chưa ghi nhận tiền. Vui lòng đợi thêm hoặc kiểm tra lại ngân hàng!"
                        _isBannerSuccess.value = false
                        _showBanner.value = true
                    }
                },
                onFailure = { exception ->
                    _bannerMessage.value = exception.message ?: "Failed to check payment status."
                    _isBannerSuccess.value = false
                    _showBanner.value = true
                }
            )

            _isCheckingPayment.value = false
        }
    }

    fun showCustomBanner(message: String, isSuccess: Boolean) {
        _bannerMessage.value = message
        _isBannerSuccess.value = isSuccess
        _showBanner.value = true
    }
}