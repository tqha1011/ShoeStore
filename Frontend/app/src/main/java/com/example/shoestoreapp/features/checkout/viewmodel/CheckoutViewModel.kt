package com.example.shoestoreapp.features.checkout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestoreapp.features.checkout.data.models.CurrencyType
import com.example.shoestoreapp.features.checkout.data.models.DeliveryAddress
import com.example.shoestoreapp.features.checkout.data.models.OrderSummary
import com.example.shoestoreapp.features.checkout.data.models.PaymentMethod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.round

/**
 * CheckoutViewModel: Quản lý logic và state của trang Checkout.
 *
 * Chức năng chính:
 * - Quản lý state UI (currency, delivery address, payment method, order summary)
 * - Tính toán giá tiền theo loại tiền tệ đã chọn
 * - Cập nhật OrderSummary khi thay đổi currency
 * - Xử lý các tương tác từ user (chọn currency, chọn địa chỉ, chọn phương thức thanh toán)
 * - Xử lý áp dụng mã khuyến mãi
 *
 * State Management:
 * - Sử dụng MutableStateFlow để quản lý các state có thể thay đổi
 * - Expose StateFlow (read-only) để UI observe các thay đổi
 * - Tự động cập nhật order summary khi currency thay đổi
 */
class CheckoutViewModel : ViewModel() {

    // ============ STATE MANAGEMENT ============

    // Loại tiền tệ hiện tại
    private val _selectedCurrency = MutableStateFlow(CurrencyType.USD)
    val selectedCurrency: StateFlow<CurrencyType> = _selectedCurrency.asStateFlow()

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
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Thông báo lỗi
    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage.asStateFlow()

    // Danh sách các payment methods có sẵn
    private val _availablePaymentMethods = MutableStateFlow(
        listOf(
            PaymentMethod(
                id = "1",
                displayName = "Visa ending in 4242",
                cardLast4 = "4242",
                expiryDate = "12/26",
                isDefault = true
            ),
            PaymentMethod(
                id = "2",
                displayName = "Apple Pay",
                isDefault = false
            )
        )
    )
    val availablePaymentMethods: StateFlow<List<PaymentMethod>> = _availablePaymentMethods.asStateFlow()

    // ============ INITIALIZATION ============

    init {
        viewModelScope.launch{
            loadCheckoutData()
        }
    }

    // ============ PUBLIC FUNCTIONS ============

    /**
     * Thay đổi loại tiền tệ và cập nhật giá tiền tương ứng.
     *
     * @param currency - Loại tiền tệ mới
     *
     * Quy trình:
     * 1. Cập nhật _selectedCurrency
     * 2. Gọi recalculateOrderSummary() để tính toán lại giá tiền
     */
    fun selectCurrency(currency: CurrencyType) {
        viewModelScope.launch {
            _selectedCurrency.value = currency
            recalculateOrderSummary()
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
     * Áp dụng mã khuyến mãi và cập nhật giá tiền.
     *
     * @param promoCode - Mã khuyến mãi
     *
     * Logic giả định:
     * - Mã "NIKE10" giảm 10% = $17.28 (tính từ $172.80)
     * - Mã "NIKE20" giảm 20% = $34.56
     * - Mã không hợp lệ sẽ hiển thị thông báo lỗi
     */
    fun applyPromoCode(promoCode: String) {
        viewModelScope.launch {
            if (promoCode.isBlank()) {
                _errorMessage.value = "Vui lòng nhập mã khuyến mãi"
                return@launch
            }

            // Giả lập kiểm tra mã khuyến mãi từ API
            val discountPercent = when (promoCode.uppercase()) {
                "NIKE10" -> 0.10
                "NIKE20" -> 0.20
                "SAVE15" -> 0.15
                else -> {
                    _errorMessage.value = "Mã khuyến mãi không hợp lệ"
                    return@launch
                }
            }

            // Cập nhật order summary với discount
            val currentSummary = _orderSummary.value
            val discountAmount = currentSummary.total * discountPercent
            val newTotal = currentSummary.total - discountAmount

            _orderSummary.value = currentSummary.copy(
                promoCode = promoCode.uppercase(),
                discountAmount = discountAmount,
                total = newTotal
            )

            _errorMessage.value = "" // Xóa thông báo lỗi nếu có
        }
    }

    /**
     * Xoá mã khuyến mãi và khôi phục giá tiền gốc.
     */
    fun removePromoCode() {
        viewModelScope.launch {
            recalculateOrderSummary()
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
    fun completePurchase(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Giả lập API call
                // Thực tế sẽ gọi PaymentRepository.processPayment()
                simulatePaymentProcessing()

                _isLoading.value = false
                onSuccess()
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = e.message ?: "Lỗi không xác định"
                onError(_errorMessage.value)
            }
        }
    }

    // ============ PRIVATE FUNCTIONS ============

    /**
     * Load dữ liệu checkout ban đầu.
     * Thực tế sẽ gọi các repository để lấy dữ liệu từ API.
     */
    private suspend fun loadCheckoutData() {
        try {
            // Giả lập loading từ API
            _isLoading.value = true
            // Ở đây sẽ gọi repository để lấy:
            // - Danh sách sản phẩm trong giỏ
            // - Địa chỉ giao hàng mặc định
            // - Phương thức thanh toán mặc định
            _isLoading.value = false
        } catch (e: Exception) {
            _isLoading.value = false
            _errorMessage.value = "Lỗi tải dữ liệu"
        }
    }

    /**
     * Tính toán lại tóm tắt đơn hàng dựa trên currency hiện tại.
     *
     * Công thức:
     * - basePrice (USD): $160.00
     * - convertedPrice = basePrice * exchangeRate
     * - tax = convertedPrice * 0.08 (8% tax)
     * - total = convertedPrice + tax
     *
     * Ví dụ:
     * - USD: $160.00 -> tax $12.80 -> total $172.80
     * - VND: ₫3,920,000 -> tax ₫313,600 -> total ₫4,233,600
     * - EUR: €147.20 -> tax €11.78 -> total €159.00
     */
    private suspend fun recalculateOrderSummary() {
        val basePriceUSD = 160.00
        val taxRatePercent = 0.08

        val currency = _selectedCurrency.value
        val convertedSubtotal = basePriceUSD * currency.exchangeRate
        val convertedTax = convertedSubtotal * taxRatePercent
        val convertedTotal = convertedSubtotal + convertedTax

        _orderSummary.value = OrderSummary(
            subtotal = roundToDecimalPlaces(convertedSubtotal, currency.decimalPlaces),
            shipping = 0.0,
            tax = roundToDecimalPlaces(convertedTax, currency.decimalPlaces),
            total = roundToDecimalPlaces(convertedTotal, currency.decimalPlaces),
            promoCode = "",
            discountAmount = 0.0
        )
    }

    /**
     * Làm tròn số theo số chữ số thập phân.
     */
    private fun roundToDecimalPlaces(value: Double, places: Int): Double {
        if (places <= 0) return round(value)
        val multiplier = Math.pow(10.0, places.toDouble())
        return round(value * multiplier) / multiplier
    }

    /**
     * Giả lập xử lý thanh toán.
     * Thực tế sẽ gọi payment gateway API.
     */
    private suspend fun simulatePaymentProcessing() {
        // Giả lập network delay
        kotlinx.coroutines.delay(2000)
        // Thực tế: throw exception nếu thanh toán thất bại
    }
}

