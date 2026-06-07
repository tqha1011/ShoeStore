package com.example.shoestoreapp.features.user.cart.data.repositories

import com.example.shoestoreapp.core.networks.RetrofitInstance
import com.example.shoestoreapp.core.utils.ApiErrorHandler
import com.example.shoestoreapp.features.user.cart.data.remote.AddCartItemRequest
import com.example.shoestoreapp.features.user.cart.data.remote.CartApi
import com.example.shoestoreapp.features.user.cart.data.remote.CartItemResponseDto
import com.example.shoestoreapp.features.user.cart.data.remote.CartResponseDto
import com.example.shoestoreapp.features.user.cart.data.remote.UpdateCartItemRequest
import com.example.shoestoreapp.features.user.checkout.data.remote.CheckOutRequestDto
import retrofit2.Response

/**
 * CartRepositoryImpl: Triển khai gọi API cho giỏ hàng.
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
                    ?: Result.failure(CartRepositoryException.Unknown(CartRepositoryException.EMPTY_RESPONSE_BODY))
            } else {
                Result.failure(response.toRepositoryException(isFetch = true))
            }
        } catch (e: Exception) {
            Result.failure(CartRepositoryException.Unknown(e.message ?: CartRepositoryException.FETCH_ERROR_UNKNOWN))
        }
    }

    override suspend fun addToCart(variantPublicId: String, quantity: Int): Result<CartItemResponseDto> {
        if (quantity <= 0) {
            return Result.failure(CartRepositoryException.BadRequest("Quantity must be greater than 0."))
        }

        return try {
            val response = cartApi.addToCart(
                request = AddCartItemRequest(variantPublicId = variantPublicId, quantity = quantity)
            )

            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(CartRepositoryException.Unknown(CartRepositoryException.EMPTY_RESPONSE_BODY))
            } else {
                Result.failure(response.toRepositoryException())
            }
        } catch (e: Exception) {
            Result.failure(CartRepositoryException.Unknown(e.message ?: CartRepositoryException.ERROR_UNKNOWN))
        }
    }

    override suspend fun updateCartItemQuantity(cartItemId: String, quantity: Int): Result<CartItemResponseDto> {
        if (quantity <= 0) {
            return Result.failure(CartRepositoryException.BadRequest("Quantity must be greater than 0."))
        }

        return try {
            val response = cartApi.updateCartItem(
                request = UpdateCartItemRequest(cartItemId = cartItemId, quantity = quantity)
            )

            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(CartRepositoryException.Unknown(CartRepositoryException.EMPTY_RESPONSE_BODY))
            } else {
                Result.failure(response.toRepositoryException())
            }
        } catch (e: Exception) {
            Result.failure(CartRepositoryException.Unknown(e.message ?: CartRepositoryException.ERROR_UNKNOWN))
        }
    }

    override suspend fun removeCartItems(cartItemIds: List<String>): Result<Unit> {
        if (cartItemIds.isEmpty()) {
            return Result.failure(CartRepositoryException.BadRequest(CartRepositoryException.REMOVE_ERROR_EMPTY))
        }

        return try {
            val response = cartApi.removeFromCart(cartItemList = cartItemIds)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(response.toRepositoryException())
            }
        } catch (e: Exception) {
            Result.failure(CartRepositoryException.Unknown(e.message ?: CartRepositoryException.ERROR_UNKNOWN))
        }
    }

    private fun <T> Response<T>.toRepositoryException(isFetch: Boolean = false): CartRepositoryException {
        val backendMessage = ApiErrorHandler.extractErrorMessage(this)

        return when (code()) {
            400 -> CartRepositoryException.BadRequest(backendMessage ?: CartRepositoryException.ERROR_BAD_REQUEST)
            401 -> CartRepositoryException.Unauthorized(
                backendMessage ?: if (isFetch) CartRepositoryException.FETCH_ERROR_UNAUTHORIZED else CartRepositoryException.ERROR_UNAUTHORIZED
            )
            404 -> CartRepositoryException.NotFound(
                backendMessage ?: if (isFetch) CartRepositoryException.FETCH_ERROR_NOT_FOUND else CartRepositoryException.ERROR_NOT_FOUND
            )
            500 -> CartRepositoryException.ServerError(
                backendMessage ?: if (isFetch) CartRepositoryException.FETCH_ERROR_SERVER else CartRepositoryException.ERROR_SERVER
            )
            else -> CartRepositoryException.Unknown(
                backendMessage ?: if (isFetch) "${CartRepositoryException.FETCH_ERROR_UNKNOWN} (HTTP ${code()})" else "${CartRepositoryException.ERROR_UNKNOWN} (HTTP ${code()})"
            )
        }
    }
}