package com.example.shoestoreapp.features.checkout.data.models

/**
 * Data class đại diện cho thông tin tóm tắt đơn hàng.
 *
 * @param subtotal - Tổng tiền trước thuế
 * @param shipping - Tiền giao hàng
 * @param tax - Tiền thuế
 * @param total - Tổng tiền sau thuế
 * @param promoCode - Mã khuyến mãi (nếu có)
 * @param discountAmount - Số tiền giảm giá
 */
data class OrderSummary(
    val subtotal: Double = 160.00,
    val shipping: Double = 0.0,
    val tax: Double = 12.80,
    val total: Double = 172.80,
    val promoCode: String = "",
    val discountAmount: Double = 0.0
) {
    fun getDisplayShipping(): String {
        return if (shipping == 0.0) "Free" else shipping.toString()
    }
}

