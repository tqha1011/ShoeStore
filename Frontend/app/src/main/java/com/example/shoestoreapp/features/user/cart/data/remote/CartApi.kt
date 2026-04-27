package com.example.shoestoreapp.features.user.cart.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT

/**
 * CartApi: Interface định nghĩa các endpoint liên quan đến giỏ hàng
 * 
 * Endpoints:
 * - POST /api/cart - Thêm sản phẩm vào giỏ (AddCartItemDto)
 * - PUT /api/cart - Cập nhật item trong giỏ (UpdateCartItemDto)
 * - POST /api/cart/remove-items - Xóa items khỏi giỏ (List<UUID>)
 * 
 * Note:
 * - Tất cả endpoints require @Authorize(Roles = "User")
 * - Token phải được gửi trong Authorization header
 */
interface CartApi {

    /**
     * Thêm sản phẩm vào giỏ hàng
     * 
     * @param request - body chứa variantPublicId, quantity
     * @return Response chứa CartItemResponseDto
     */
    @POST("api/cart")
    suspend fun addToCart(
        @Body request: AddCartItemRequest
    ): Response<CartItemResponseDto>

    /**
     * Cập nhật item trong giỏ hàng (variant và/hoặc quantity)
     * 
     * @param dto - UpdateCartItemDto chứa cartItemId, newProductVariantId, quantity
     * @return Response chứa CartItemResponseDto
     */
    @PUT("api/cart")
    suspend fun updateCartItem(
        @Body dto: UpdateCartItemDto
    ): Response<CartItemResponseDto>

    /**
     * Xóa một hoặc nhiều items khỏi giỏ hàng
     * 
     * @param cartItemList - List<String> chứa GUIDs của các cart items cần xóa
     * @return Response chứa success message
     */
    @POST("api/cart/remove-items")
    suspend fun removeFromCart(
        @Body cartItemList: List<String>  // Changed from List<UUID> to List<String>
    ): Response<CartRemoveResponse>
}


