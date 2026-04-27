package com.example.shoestoreapp.features.user.cart.data.repositories

import com.example.shoestoreapp.core.networks.RetrofitInstance
import com.example.shoestoreapp.features.user.cart.data.remote.AddCartItemRequest
import com.example.shoestoreapp.features.user.cart.data.remote.CartApi
import com.example.shoestoreapp.features.user.cart.data.remote.CartItemResponseDto
import com.example.shoestoreapp.features.user.cart.data.remote.UpdateCartItemDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import retrofit2.Response

/**
 * CartRepository: Contract cho các thao tác giỏ hàng dùng ở presentation layer.
 */
interface CartRepository {

    /**
     * Thêm sản phẩm vào giỏ hàng.
     *
     * Trả về Result.success khi API 200.
     * Trả về Result.failure với CartRepositoryException khi API lỗi.
     */
    suspend fun addToCart(variantPublicId: String, quantity: Int): Result<CartItemResponseDto>
}

sealed class CartRepositoryException(message: String) : Exception(message) {
    class BadRequest(message: String = ERROR_BAD_REQUEST) : CartRepositoryException(message)
    class Unauthorized(message: String = ERROR_UNAUTHORIZED) : CartRepositoryException(message)
    class NotFound(message: String = ERROR_NOT_FOUND) : CartRepositoryException(message)
    class ServerError(message: String = ERROR_SERVER) : CartRepositoryException(message)
    class Unknown(message: String = ERROR_UNKNOWN) : CartRepositoryException(message)
}

private const val ERROR_BAD_REQUEST = "The requested quantity exceeds available stock."
private const val ERROR_UNAUTHORIZED = "Your session has expired. Please sign in again."
private const val ERROR_NOT_FOUND = "The selected product variant was not found."
private const val ERROR_SERVER = "Server error. Please try again in a moment."
private const val ERROR_UNKNOWN = "Something went wrong while updating cart."

/**
 * CartRepositoryImpl: Triển khai gọi API cho giỏ hàng.
 *
 * Auth token được inject tự động bởi AuthInterceptor.
 */
class CartRepositoryImpl(
    private val cartApi: CartApi = RetrofitInstance.cartApi
) : CartRepository {

    /**
     * Thêm sản phẩm vào giỏ hàng
     * 
     * Gọi API: POST /api/cart
     * Request: AddCartItemDto { userId, variantId, quantity }
     * Response: CartItemResponseDto
     * 
     * @param variantPublicId - GUID của product variant
     * @param quantity - Số lượng sản phẩm
     * @return Result<CartItemResponseDto> - Chi tiết item hoặc lỗi đã map theo HTTP status
     */
    override suspend fun addToCart(
        variantPublicId: String,
        quantity: Int
    ): Result<CartItemResponseDto> {
        if (quantity <= 0) {
            return Result.failure(CartRepositoryException.BadRequest("Quantity must be greater than 0."))
        }

        return try {
            val response = cartApi.addToCart(
                request = AddCartItemRequest(
                    variantPublicId = variantPublicId,
                    quantity = quantity
                )
            )

            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(CartRepositoryException.Unknown("Empty response body."))
            } else {
                Result.failure(response.toRepositoryException())
            }
        } catch (e: Exception) {
            Result.failure(CartRepositoryException.Unknown(e.message ?: ERROR_UNKNOWN))
        }
    }

    private fun <T> Response<T>.toRepositoryException(): CartRepositoryException {
        val backendMessage = errorBody()?.string()?.takeIf { it.isNotBlank() }

        return when (code()) {
            400 -> CartRepositoryException.BadRequest(backendMessage ?: ERROR_BAD_REQUEST)
            401 -> CartRepositoryException.Unauthorized(backendMessage ?: ERROR_UNAUTHORIZED)
            404 -> CartRepositoryException.NotFound(backendMessage ?: ERROR_NOT_FOUND)
            500 -> CartRepositoryException.ServerError(backendMessage ?: ERROR_SERVER)
            else -> CartRepositoryException.Unknown(backendMessage ?: "$ERROR_UNKNOWN (HTTP ${code()})")
        }
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
                val response = cartApi.updateCartItem(dto = request)

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
                val response = cartApi.removeFromCart(cartItemList = cartItemIds)
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

