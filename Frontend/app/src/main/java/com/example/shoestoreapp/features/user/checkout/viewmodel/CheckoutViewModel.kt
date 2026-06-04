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
                // Tìm địa chỉ có isDefault = true, nếu không có thì lấy cái đầu tiên (nếu có)
                val default = addresses.find { it.isDefault } ?: addresses.firstOrNull()
                default?.let {
                    _deliveryAddress.value = _deliveryAddress.value.copy(
                        fullAddress = it.address // Gán chuỗi address từ Backend trả về
                    )
                }
            }.onFailure {
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
                        _bannerMessage.value = parsedMessage
                        _isBannerSuccess.value = false
                        _showBanner.value = true
                        _placeOrderState.value = PlaceOrderUiState.Error(parsedMessage)

                        _selectedProductVoucher.value = null
                        _selectedShippingVoucher.value = null
                        prepareCheckoutSession()

                    } else {
                        val msg = exception.message ?: "Unknown error during checkout preparation"
                        _errorMessage.value = msg
                        _bannerMessage.value = msg
                        _isBannerSuccess.value = false
                        _showBanner.value = true
                    }
                }
            } catch (e: Exception) {
                _isLoading.value = false
                val msg = e.message ?: "Unknown error during checkout preparation"
                _errorMessage.value = msg
                _bannerMessage.value = msg
                _isBannerSuccess.value = false
                _showBanner.value = true
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

            try {
                val result = checkoutRepository.placeOrder(fromUserCart, requestDto)
                result.onSuccess { invoice ->
                    _placeOrderState.value = PlaceOrderUiState.Success(invoice)
                    CheckoutSession.pendingItems = emptyList()
                }.onFailure { exception ->

                    // LỖI KHI BẤM THANH TOÁN
                    if (exception is HttpException && exception.code() == 400) {
                        val parsedMessage = parseErrorMessage(exception)
                        _bannerMessage.value = parsedMessage
                        _isBannerSuccess.value = false
                        _showBanner.value = true
                        _placeOrderState.value = PlaceOrderUiState.Error(parsedMessage)
                    } else {
                        // Lỗi khác
                        val msg = exception.message ?: "Unknown error during placing order"
                        _bannerMessage.value = msg
                        _isBannerSuccess.value = false
                        _showBanner.value = true
                        _placeOrderState.value = PlaceOrderUiState.Error(msg)
                    }
                }
            } catch (e: Exception) {
                val msg = e.message ?: "Unknown error during placing order"
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