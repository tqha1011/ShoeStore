package com.example.shoestoreapp.features.checkout.data.models

/**
 * Enum định nghĩa các loại tiền tệ được hỗ trợ trong ứng dụng.
 *
 * Mỗi loại tiền tệ có:
 * - code: Mã tiền tệ (USD, VND, EUR)
 * - symbol: Ký hiệu tiền tệ ($, ₫, €)
 * - exchangeRate: Tỷ giá so với USD (tính từ USD = 1.0)
 * - decimalPlaces: Số chữ số thập phân để hiển thị
 */
enum class CurrencyType(
    val code: String,
    val symbol: String,
    val exchangeRate: Double,
    val decimalPlaces: Int
) {
    USD(code = "USD", symbol = "$", exchangeRate = 1.0, decimalPlaces = 2),
    VND(code = "VND", symbol = "₫", exchangeRate = 24500.0, decimalPlaces = 0),
    EUR(code = "EUR", symbol = "€", exchangeRate = 0.92, decimalPlaces = 2);
}

