package com.example.shoestoreapp.features.checkout.data.models

/**
 * Data class đại diện cho phương thức thanh toán.
 *
 * @param id - ID phương thức thanh toán
 * @param type - Loại thanh toán (VISA, MASTERCARD, APPLE_PAY, GOOGLE_PAY, etc.)
 * @param displayName - Tên hiển thị (VD: "Visa ending in 4242")
 * @param cardLast4 - 4 chữ số cuối của thẻ
 * @param expiryDate - Ngày hết hạn (MM/YY)
 * @param isDefault - Có phải phương thức mặc định hay không
 */
data class PaymentMethod(
    val id: String = "",
    val type: PaymentType = PaymentType.VISA,
    val displayName: String = "Visa ending in 4242",
    val cardLast4: String = "4242",
    val expiryDate: String = "12/26",
    val isDefault: Boolean = true
)

/**
 * Enum định nghĩa các loại thanh toán được hỗ trợ.
 */
enum class PaymentType {
    VISA,
    MASTERCARD,
    AMERICAN_EXPRESS,
    APPLE_PAY,
    GOOGLE_PAY
}

