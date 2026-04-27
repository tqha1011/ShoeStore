package com.example.shoestoreapp.features.user.cart.data.remote

/**
 * Request body cho POST /api/cart.
 */
data class AddCartItemRequest(
    val variantPublicId: String,
    val quantity: Int
)

/**
 * UpdateCartItemDto: Request DTO để cập nhật item trong giỏ hàng
 * 
 * @param cartItemId - ID của cart item cần cập nhật (GUID)
 * @param newProductVariantId - ID của product variant mới (GUID)
 * @param quantity - Số lượng mới
 */
data class UpdateCartItemDto(
    val cartItemId: String,         // GUID
    val newProductVariantId: String, // GUID
    val quantity: Int
)

/**
 * Response 200 cho add/update cart item.
 */
data class CartItemResponse(
    val cartItemId: String,
    val productVariantId: String,
    val productName: String,
    val imageUrl: String,
    val price: Double,
    val stock: Int,
    val sizeId: Int,
    val size: Int,
    val colorId: Int,
    val colorName: String,
    val quantity: Int,
    val isSelling: Boolean
)

typealias CartItemResponseDto = CartItemResponse

/**
 * CartRemoveResponse: Response từ API khi xóa cart items
 */
data class CartRemoveResponse(
    val message: String,
    val success: Boolean
)

