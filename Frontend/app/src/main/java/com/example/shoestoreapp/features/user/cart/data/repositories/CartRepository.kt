package com.example.shoestoreapp.features.user.cart.data.repositories

import com.example.shoestoreapp.core.networks.RetrofitInstance
import com.example.shoestoreapp.features.user.cart.data.remote.AddCartItemRequest
import com.example.shoestoreapp.features.user.cart.data.remote.CartApi
import com.example.shoestoreapp.features.user.cart.data.remote.CartItemResponseDto
import com.example.shoestoreapp.features.user.cart.data.remote.CartResponseDto
import com.example.shoestoreapp.features.user.cart.data.remote.UpdateCartItemRequest
import com.example.shoestoreapp.features.user.checkout.data.remote.CheckOutRequestDto
import retrofit2.Response

/**
 * CartRepository: Contract cho các thao tác giỏ hàng dùng ở presentation layer.
 */
interface CartRepository {

    /**
     * Lấy toàn bộ cart items của user hiện tại.
     */
    suspend fun getCartItems(): Result<CartResponseDto>

    /**
     * Thêm sản phẩm vào giỏ hàng.
     *
     * Trả về Result.success khi API 200.
     * Trả về Result.failure với CartRepositoryException khi API lỗi.
     */
    suspend fun addToCart(variantPublicId: String, quantity: Int): Result<CartItemResponseDto>

    /**
     * Cập nhật số lượng của cart item.
     */
    suspend fun updateCartItemQuantity(cartItemId: String, quantity: Int): Result<CartItemResponseDto>

    /**
     * Xóa một hoặc nhiều items khỏi giỏ hàng.
     */
    suspend fun removeCartItems(cartItemIds: List<String>): Result<Unit>
    fun setPendingCheckout(items: List<CheckOutRequestDto>)
    fun getPendingCheckout(): List<CheckOutRequestDto>
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
private const val FETCH_ERROR_UNAUTHORIZED = "Unauthorized. Please sign in to view your cart."
private const val FETCH_ERROR_NOT_FOUND = "User not found. Unable to load cart items."
private const val FETCH_ERROR_SERVER = "Server error while loading cart items. Please try again."
private const val FETCH_ERROR_UNKNOWN = "Unable to load cart items right now."
private const val REMOVE_ERROR_EMPTY = "No cart items provided for removal."

/**
 * CartRepositoryImpl: Triển khai gọi API cho giỏ hàng.
 *
 * Auth token được inject tự động bởi AuthInterceptor.
 */
class CartRepositoryImpl(
    private val cartApi: CartApi = RetrofitInstance.cartApi
) : CartRepository {

    private var pendingCheckoutItems: List<CheckOutRequestDto> = emptyList()

    override fun setPendingCheckout(items: List<CheckOutRequestDto>) {
        pendingCheckoutItems = items
    }

    override fun getPendingCheckout(): List<CheckOutRequestDto> {
        return pendingCheckoutItems
    }

    override suspend fun getCartItems(): Result<CartResponseDto> {
        return try {
            val response = cartApi.getCartItems()

            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(CartRepositoryException.Unknown("Empty response body."))
            } else {
                val backendMessage = response.errorBody()?.string()?.takeIf { it.isNotBlank() }
                val exception = when (response.code()) {
                    401 -> CartRepositoryException.Unauthorized(backendMessage ?: FETCH_ERROR_UNAUTHORIZED)
                    404 -> CartRepositoryException.NotFound(backendMessage ?: FETCH_ERROR_NOT_FOUND)
                    500 -> CartRepositoryException.ServerError(backendMessage ?: FETCH_ERROR_SERVER)
                    else -> CartRepositoryException.Unknown(
                        backendMessage ?: "$FETCH_ERROR_UNKNOWN (HTTP ${response.code()})"
                    )
                }
                Result.failure(exception)
            }
        } catch (e: Exception) {
            Result.failure(CartRepositoryException.Unknown(e.message ?: FETCH_ERROR_UNKNOWN))
        }
    }

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

    override suspend fun updateCartItemQuantity(
        cartItemId: String,
        quantity: Int
    ): Result<CartItemResponseDto> {
        if (quantity <= 0) {
            return Result.failure(CartRepositoryException.BadRequest("Quantity must be greater than 0."))
        }

        return try {
            val response = cartApi.updateCartItem(
                request = UpdateCartItemRequest(
                    cartItemId = cartItemId,
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

    override suspend fun removeCartItems(cartItemIds: List<String>): Result<Unit> {
        if (cartItemIds.isEmpty()) {
            return Result.failure(CartRepositoryException.BadRequest(REMOVE_ERROR_EMPTY))
        }

        return try {
            val response = cartApi.removeFromCart(cartItemList = cartItemIds)
            if (response.isSuccessful) {
                Result.success(Unit)
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
}
