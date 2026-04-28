package com.example.shoestoreapp.features.user.cart.data.remote

/**
 * Request body cho POST /api/cart.
 */
data class AddCartItemRequest(
    val variantPublicId: String,
    val quantity: Int
)


/**
 * Response 200 cho add/update cart item.
 */
data class CartItemResponseDto(
    val cartItemId: String,
    val productVariantId: String,
    val productName: String,
    val brand: String?,
    val imageUrl: String?,
    val price: Double,
    val stock: Int,
    val sizeId: Int,
    val size: Int,
    val colorId: Int,
    val colorName: String,
    val quantity: Int,
    val isSelling: Boolean
)

/**
 * CartRemoveResponse: Response từ API khi xóa cart items
 */
data class CartRemoveResponse(
    val message: String,
    val success: Boolean
)

/**
 * UpdateCartItemRequest: Request body cho PUT /api/cart (update quantity).
 *
 * @param cartItemId - ID của cart item cần cập nhật (GUID)
 * @param quantity - Số lượng mới
 */
data class UpdateCartItemRequest(
    val cartItemId: String,
    val quantity: Int
)
