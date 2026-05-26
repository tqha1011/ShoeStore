package com.example.shoestoreapp.features.user.cart.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

/**
 * CartApi: Interface định nghĩa các endpoint liên quan đến giỏ hàng
 * 
 * Endpoints:
 * - POST /api/cart - Thêm sản phẩm vào giỏ (AddCartItemDto)
 * - PUT /api/cart - Cập nhật item trong giỏ (UpdateCartItemRequest)
 * - POST /api/cart/remove-items - Xóa items khỏi giỏ (List<UUID>)
 * 
 * Note:
 * - Tất cả endpoints require @Authorize(Roles = "User")
 * - Token phải được gửi trong Authorization header
 */
interface CartApi {

    /**
     * Lấy toàn bộ cart items của user hiện tại.
     *
     * Endpoint: GET /api/cart/user-cart-items
     */
    @GET("api/cart/user-cart-items")
    suspend fun getCartItems(): Response<CartResponseDto>

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
     * Cập nhật item trong giỏ hàng (quantity)
     *
     * @param request - UpdateCartItemRequest chứa cartItemId, quantity
     * @return Response chứa CartItemResponseDto
     */
    @PUT("api/cart")
    suspend fun updateCartItem(
        @Body request: UpdateCartItemRequest
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
    ): Response<Unit>
}
