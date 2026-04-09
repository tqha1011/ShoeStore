package com.example.shoestoreapp.features.cart.data.models

/**
 * Data class representing a cart item.
 * Extends Product with quantity and size information.
 *
 * @param id - ID giỏ hàng item (unique)
 * @param productId - ID sản phẩm
 * @param name - Tên sản phẩm
 * @param imageUrl - URL ảnh sản phẩm
 * @param description - Mô tả sản phẩm
 * @param price - Giá đơn vị sản phẩm
 * @param quantity - Số lượng sản phẩm trong giỏ
 * @param size - Size sản phẩm (VD: "10.5", "9", "11")
 * @param stock - Số lượng còn lại trong kho
 */
data class CartItem(
    val id: Int,
    val productId: Int,
    val name: String,
    val imageUrl: String,
    val description: String,
    val price: Double,
    val quantity: Int,
    val size: String,
    val stock: Int
) {
    /**
     * Tính tổng tiền cho item này
     */
    fun getTotalPrice(): Double = price * quantity
}

