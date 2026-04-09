package com.example.shoestoreapp.features.cart.data.models

/**
 * Data class representing cart summary information.
 *
 * @param subtotal - Tổng tiền hàng (trước shipping & tax)
 * @param shippingCost - Chi phí vận chuyển ước tính
 * @param tax - Thuế ước tính
 * @param itemCount - Số lượng loại sản phẩm (không phải tổng quantity)
 */
data class CartSummary(
    val subtotal: Double = 0.0,
    val shippingCost: Double = 15.0,
    val tax: Double = 0.0,
    val itemCount: Int = 0
) {
    /**
     * Tính tổng tiền cuối cùng
     */
    fun getTotal(): Double = subtotal + shippingCost + tax
}

