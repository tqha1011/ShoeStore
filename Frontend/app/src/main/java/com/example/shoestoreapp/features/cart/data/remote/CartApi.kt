package com.example.shoestoreapp.features.cart.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
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
     * @param authorization - Bearer token từ Authentication
     * @param dto - AddCartItemDto chứa userId, variantId, quantity
     * @return Response chứa CartItemResponseDto
     */
    @POST("api/cart")
    suspend fun addToCart(
        @Header("Authorization") authorization: String,
        @Body dto: AddCartItemDto
    ): Response<CartItemResponseDto>

    /**
     * Cập nhật item trong giỏ hàng (variant và/hoặc quantity)
     * 
     * @param authorization - Bearer token từ Authentication
     * @param dto - UpdateCartItemDto chứa cartItemId, newProductVariantId, quantity
     * @return Response chứa CartItemResponseDto
     */
    @PUT("api/cart")
    suspend fun updateCartItem(
        @Header("Authorization") authorization: String,
        @Body dto: UpdateCartItemDto
    ): Response<CartItemResponseDto>

    /**
     * Xóa một hoặc nhiều items khỏi giỏ hàng
     * 
     * @param authorization - Bearer token từ Authentication
     * @param cartItemList - List<String> chứa GUIDs của các cart items cần xóa
     * @return Response chứa success message
     */
    @POST("api/cart/remove-items")
    suspend fun removeFromCart(
        @Header("Authorization") authorization: String,
        @Body cartItemList: List<String>  // Changed from List<UUID> to List<String>
    ): Response<CartRemoveResponse>
}


