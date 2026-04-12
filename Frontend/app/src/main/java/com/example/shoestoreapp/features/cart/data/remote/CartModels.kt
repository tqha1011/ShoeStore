package com.example.shoestoreapp.features.cart.data.remote

/**
 * AddCartItemDto: Request DTO để thêm sản phẩm vào giỏ hàng
 * 
 * @param userId - ID của user
 * @param variantId - ID của product variant (GUID)
 * @param quantity - Số lượng sản phẩm
 */
data class AddCartItemDto(
    val userId: String,
    val variantId: String,  // GUID
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
 * CartItemResponseDto: Response từ API khi add/update cart item
 * 
 * Match với Backend UserCartItemResponseDto:
 * @param cartItemId - ID của cart item (GUID)
 * @param productVariantId - ID của product variant (GUID, nullable)
 * @param productName - Tên sản phẩm
 * @param brand - Thương hiệu
 * @param imageUrl - URL ảnh
 * @param price - Giá sản phẩm
 * @param stock - Tồn kho
 * @param sizeId - ID kích cỡ
 * @param size - Giá trị kích cỡ
 * @param colorId - ID màu sắc
 * @param colorName - Tên màu sắc
 * @param quantity - Số lượng trong giỏ
 */
data class CartItemResponseDto(
    val cartItemId: String,           // GUID
    val productVariantId: String?,    // GUID nullable
    val productName: String?,
    val brand: String?,
    val imageUrl: String?,
    val price: Double,
    val stock: Int,
    val sizeId: Int,
    val size: Int,
    val colorId: Int,
    val colorName: String?,
    val quantity: Int
)

/**
 * CartRemoveResponse: Response từ API khi xóa cart items
 */
data class CartRemoveResponse(
    val message: String,
    val success: Boolean
)

