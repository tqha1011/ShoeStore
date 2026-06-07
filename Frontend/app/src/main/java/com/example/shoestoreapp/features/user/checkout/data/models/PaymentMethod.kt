package com.example.shoestoreapp.features.user.checkout.data.models

/**
 * Data class đại diện cho phương thức thanh toán.
 *
 * @param id - ID phương thức thanh toán
 * @param type - Loại thanh toán (VISA, MASTERCARD, APPLE_PAY, GOOGLE_PAY, etc.)
 * @param displayName - Tên hiển thị (VD: "Visa ending in 4242")
 * @param expiryDate - Ngày hết hạn (MM/YY)
 * @param isDefault - Có phải phương thức mặc định hay không
 */
data class PaymentMethod(
    val id: Int = 1,
    val type: PaymentType = PaymentType.SePay,
    val displayName: String = "SePay",
    val expiryDate: String = "12/26",
    val isDefault: Boolean = true
)

/**
 * Enum định nghĩa các loại thanh toán được hỗ trợ.
 */
enum class PaymentType {
    SePay,
    COD
}

