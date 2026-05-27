package com.example.shoestoreapp.features.user.checkout.data.models

/**
 * Enum định nghĩa các loại tiền tệ được hỗ trợ trong ứng dụng.
 *
 * Mỗi loại tiền tệ có:
 * - code: Mã tiền tệ (VND)
 * - symbol: Ký hiệu tiền tệ (₫)
 * - decimalPlaces: Số chữ số thập phân để hiển thị
 */
enum class CurrencyType(
    val code: String,
    val symbol: String,
    val decimalPlaces: Int
) {
    VND(code = "VND", symbol = "₫", decimalPlaces = 0);
}
