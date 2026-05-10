package com.example.shoestoreapp.features.user.checkout.data.remote

import com.example.shoestoreapp.features.user.checkout.data.models.OrderSummary

data class CheckOutRequestDto(
    val variantId: String,
    val quantity: Int
)

data class CheckOutResponseDto(
    val items: List<CheckOutItemDto>,
    val summary: CheckOutSummaryDto,
    val warnings: List<String>? = emptyList()
)

data class CheckOutItemDto(
    val colorName: String?,
    val isOutOfStock: Boolean,
    val productName: String,
    val quantity: Int,
    val size: Double,
    val stockAvailable: Int,
    val subTotal: Double,
    val unitPrice: Double,
    val variantId: String
)

data class CheckOutSummaryDto(
    val finalPrice: Double,
    val totalPrice: Double
)

data class PlaceOrderRequestDto(
    val address: String,
    val fullName: String,
    val items: List<CheckOutRequestDto>,
    val paymentId: Int,
    val phoneNumber: String,
    val voucherIds: List<String>
)

/**
 * Hàm mở rộng giúp chuyển đổi từ DTO của Server sang Model của UI
 */
fun CheckOutSummaryDto.toOrderSummary(): OrderSummary {
    // Tự động tính toán số tiền được giảm (nếu có)
    val calculatedDiscount = if (this.totalPrice > this.finalPrice) {
        this.totalPrice - this.finalPrice
    } else {
        0.0
    }

    return OrderSummary(
        subtotal = this.totalPrice,   // Tổng tiền trước thuế/giảm giá
        total = this.finalPrice,      // Số tiền cuối cùng user phải trả
        shipping = 0.0,               // API chưa trả về, tạm set 0.0 (Free shipping)
        tax = 0.0,                    // API chưa trả về, tạm set 0.0
        discountAmount = calculatedDiscount,
        promoCode = ""                // Tạm thời để trống vì chưa có API áp mã
    )
}