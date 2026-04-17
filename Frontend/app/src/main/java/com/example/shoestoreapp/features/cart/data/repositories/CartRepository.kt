package com.example.shoestoreapp.features.cart.data.repositories

import com.example.shoestoreapp.core.networks.RetrofitInstance
import com.example.shoestoreapp.features.cart.data.remote.AddCartItemDto
import com.example.shoestoreapp.features.cart.data.remote.CartApi
import com.example.shoestoreapp.features.cart.data.remote.CartItemResponseDto
import com.example.shoestoreapp.features.cart.data.remote.UpdateCartItemDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

/**
 * CartRepository: Lớp này cung cấp và quản lý dữ liệu giỏ hàng từ API.
 *
 * Chức năng:
 * - CRUD operations cho cart items (add, remove, update)
 * - Gọi API với Authentication Token (Authorize required)
 * - Mapping response DTO thành domain models
 *
 * Endpoints:
 * - POST /api/cart - Thêm item vào giỏ
 * - PUT /api/cart - Cập nhật item trong giỏ
 * - POST /api/cart/remove-items - Xóa một hoặc nhiều items
 *
 * Note:
 * - Tất cả API calls require Bearer token trong Authorization header
 * - Sử dụng GUID (String) thay vì Int ID
 * - Token được lấy từ shared preferences hoặc auth service
 */
class CartRepository(
    private val cartApi: CartApi = RetrofitInstance.cartApi,
    private val userId: String = "user-id",  // API_PEND: Lấy từ shared preferences
    private val authToken: String = ""       // API_PEND: Lấy token từ auth service
) {

    private fun getAuthorizationHeader(): String = "Bearer $authToken"

    /**
     * Thêm sản phẩm vào giỏ hàng
     * 
     * Gọi API: POST /api/cart
     * Request: AddCartItemDto { userId, variantId, quantity }
     * Response: CartItemResponseDto
     * 
     * @param variantId - GUID của product variant
     * @param quantity - Số lượng sản phẩm
     * @return Flow<CartItemResponseDto?> - Chi tiết item vừa thêm hoặc null nếu thất bại
     */
    fun addToCart(variantId: String, quantity: Int): Flow<CartItemResponseDto?> = flow {
        val result = try {
            val request = AddCartItemDto(
                userId = userId,
                variantId = variantId,
                quantity = quantity
            )
            val response = cartApi.addToCart(
                authorization = getAuthorizationHeader(),
                dto = request
            )

            if (response.isSuccessful && response.body() != null) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
        emit(result)  // ← emit() OUTSIDE try-catch
    }.catch { exception ->
        exception.printStackTrace()
    }

    /**
     * Cập nhật item trong giỏ hàng (variant và/hoặc quantity)
     * 
     * Gọi API: PUT /api/cart
     * Request: UpdateCartItemDto { cartItemId, newProductVariantId, quantity }
     * Response: CartItemResponseDto
     * 
     * @param cartItemId - GUID của cart item cần cập nhật
     * @param newProductVariantId - GUID của product variant mới
     * @param newQuantity - Số lượng mới
     * @return Flow<CartItemResponseDto?> - Chi tiết item sau update hoặc null nếu thất bại
     */
    fun updateCartItem(
        cartItemId: String,
        newProductVariantId: String,
        newQuantity: Int
    ): Flow<CartItemResponseDto?> = flow {
        val result = try {
            if (newQuantity <= 0) {
                null
            } else {
                val request = UpdateCartItemDto(
                    cartItemId = cartItemId,
                    newProductVariantId = newProductVariantId,
                    quantity = newQuantity
                )
                val response = cartApi.updateCartItem(
                    authorization = getAuthorizationHeader(),
                    dto = request
                )

                if (response.isSuccessful && response.body() != null) {
                    response.body()
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
        emit(result)  // ← emit() OUTSIDE try-catch
    }.catch { exception ->
        exception.printStackTrace()
    }

    /**
     * Xóa một hoặc nhiều items khỏi giỏ hàng
     * 
     * Gọi API: POST /api/cart/remove-items
     * Request: List<String> (danh sách GUIDs của cart items)
     * Response: CartRemoveResponse
     * 
     * @param cartItemIds - List<String> chứa GUIDs của cart items cần xóa
     * @return Flow<Boolean> - true nếu xóa thành công, false nếu thất bại
     */
    fun removeFromCart(cartItemIds: List<String>): Flow<Boolean> = flow {
        val result = try {
            if (cartItemIds.isEmpty()) {
                false
            } else {
                val response = cartApi.removeFromCart(
                    authorization = getAuthorizationHeader(),
                    cartItemList = cartItemIds
                )
                response.isSuccessful
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
        emit(result)  // ← emit() OUTSIDE try-catch
    }.catch { exception ->
        exception.printStackTrace()
    }

    /**
     * Xóa một item khỏi giỏ hàng (helper function)
     * 
     * @param cartItemId - GUID của cart item cần xóa
     * @return Flow<Boolean> - true nếu xóa thành công
     */
    fun removeCartItem(cartItemId: String): Flow<Boolean> = flow {
        removeFromCart(listOf(cartItemId)).collect { result ->
            emit(result)
        }
    }
}

