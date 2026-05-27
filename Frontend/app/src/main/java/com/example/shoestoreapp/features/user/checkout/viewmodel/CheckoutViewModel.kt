package com.example.shoestoreapp.features.user.checkout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestoreapp.features.user.checkout.data.models.DeliveryAddress
import com.example.shoestoreapp.features.user.checkout.data.models.OrderSummary
import com.example.shoestoreapp.features.user.checkout.data.models.PaymentMethod
import com.example.shoestoreapp.features.user.checkout.data.models.PaymentType
import com.example.shoestoreapp.features.user.checkout.data.remote.CheckOutItemDto
import com.example.shoestoreapp.features.user.checkout.data.remote.CheckOutRequestDto
import com.example.shoestoreapp.features.user.checkout.data.remote.toOrderSummary
import com.example.shoestoreapp.features.user.checkout.data.repositories.CheckOutRepository
import com.example.shoestoreapp.features.user.checkout.data.repositories.CheckoutRepositoryImpl
import com.example.shoestoreapp.features.user.checkout.data.repositories.CheckoutSession
import com.example.shoestoreapp.features.user.checkout.data.remote.PlaceOrderRequestDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface PlaceOrderUiState {
    object Idle : PlaceOrderUiState
    object Success : PlaceOrderUiState
    object Loading : PlaceOrderUiState
    data class Error(val message: String) : PlaceOrderUiState
}
/**
 * CheckoutViewModel: Quản lý logic và state của trang Checkout.
 *
 * Chức năng chính:
 * - Quản lý state UI (delivery address, payment method, order summary)
 * - Xử lý các tương tác từ user (chọn địa chỉ, chọn phương thức thanh toán)
 * - Xử lý áp dụng mã khuyến mãi
 *
 * State Management:
 * - Sử dụng MutableStateFlow để quản lý các state có thể thay đổi
 * - Expose StateFlow (read-only) để UI observe các thay đổi
 */
class CheckoutViewModel(
    private val checkoutRepository: CheckOutRepository = CheckoutRepositoryImpl(),
) : ViewModel() {

    // ============ STATE MANAGEMENT ============

    // Địa chỉ giao hàng
    private val _deliveryAddress = MutableStateFlow(DeliveryAddress())
    val deliveryAddress: StateFlow<DeliveryAddress> = _deliveryAddress.asStateFlow()

    // Phương thức thanh toán
    private val _paymentMethod = MutableStateFlow(PaymentMethod())
    val paymentMethod: StateFlow<PaymentMethod> = _paymentMethod.asStateFlow()

    // Tóm tắt đơn hàng
    private val _orderSummary = MutableStateFlow(OrderSummary())
    val orderSummary: StateFlow<OrderSummary> = _orderSummary.asStateFlow()

    // Trạng thái loading
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // Biến quản lý trạng thái của nút Đặt hàng
    private val _placeOrderState = MutableStateFlow<PlaceOrderUiState>(PlaceOrderUiState.Idle)
    val placeOrderState = _placeOrderState.asStateFlow()


    // Thông báo lỗi
    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage.asStateFlow()

    // Danh sách sản phẩm trong giỏ hàng (Lấy từ Session chung
    private val _cartItems = MutableStateFlow<List<CheckOutItemDto>>(emptyList())
    val cartItems: StateFlow<List<CheckOutItemDto>> = _cartItems.asStateFlow()

    // Danh sách các payment methods có sẵn
    private val _availablePaymentMethods = MutableStateFlow(
        listOf(
            PaymentMethod(
                id = 1,
                displayName = "SePay",
                expiryDate = "12/26",
                type = PaymentType.SePay,
                isDefault = true
            ),
            PaymentMethod(
                id = 2,
                displayName = "COD (Cash on Delivery)",
                expiryDate = "12/26",
                type = PaymentType.COD,
                isDefault = false
            )
        )
    )
    val availablePaymentMethods: StateFlow<List<PaymentMethod>> = _availablePaymentMethods.asStateFlow()

    // ============ INITIALIZATION ============

    init {
        // Lấy đồ từ Session chung ra
        val itemsToCheckout = CheckoutSession.pendingItems

        // Gọi API
        if (itemsToCheckout.isNotEmpty()) {
            prepareCheckoutSession(itemsToCheckout)
        }
    }

    // ============ PUBLIC FUNCTIONS ============

    fun prepareCheckoutSession(cartItems: List<CheckOutRequestDto>) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = checkoutRepository.prepareCheckOut(cartItems)
                result.onSuccess { response ->
                    _orderSummary.value = response.summary.toOrderSummary()
                    _cartItems.value = response.items
                    _isLoading.value = false
                }.onFailure { exception ->
                    _isLoading.value = false
                    _errorMessage.value =
                        exception.message ?: "Unknown error during checkout preparation"
                }
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = e.message ?: "Unknown error during checkout preparation"
            }
        }
    }



    /**
     * Chọn phương thức thanh toán.
     *
     * @param paymentMethod - Phương thức thanh toán được chọn
     */
    fun selectPaymentMethod(paymentMethod: PaymentMethod) {
        viewModelScope.launch {
            _paymentMethod.value = paymentMethod
        }
    }

    /**
     * Cập nhật địa chỉ giao hàng.
     *
     * @param address - Địa chỉ giao hàng mới
     */
    fun updateDeliveryAddress(address: DeliveryAddress) {
        viewModelScope.launch {
            _deliveryAddress.value = address
        }
    }

    /**
     * Hoàn thành đơn hàng (gọi API thanh toán).
     *
     * Logic giả định:
     * - Gửi request thanh toán đến server
     * - Xử lý response (success/failure)
     * - Cập nhật UI tương ứng
     */
    fun placeOrder(
        fullName: String,
        address: String,
        phoneNumber: String,
        voucherIds: List<String> = emptyList(), // Tạm thời để trống nếu chưa làm tính năng mã giảm giá
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

            // Đóng gói dữ liệu gửi lên API
            val requestDto = PlaceOrderRequestDto(
                items = itemsToBuy,
                voucherIds = voucherIds,
                fullName = fullName,
                address = address,
                paymentId = selectedPaymentId,
                phoneNumber = phoneNumber
            )
            try {
                val result = checkoutRepository.placeOrder(fromUserCart, requestDto)
                result.onSuccess {
                    _placeOrderState.value = PlaceOrderUiState.Success
                    // Xóa session sau khi đặt hàng thành công
                    CheckoutSession.pendingItems = emptyList()
                }.onFailure { exception ->
                    _placeOrderState.value =
                        PlaceOrderUiState.Error(exception.message ?: "Unknown error during placing order")
                }
            } catch (e: Exception) {
                _placeOrderState.value =
                    PlaceOrderUiState.Error(e.message ?: "Unknown error during placing order")
            }
        }
    }

    // ============ PRIVATE FUNCTIONS ============

}
