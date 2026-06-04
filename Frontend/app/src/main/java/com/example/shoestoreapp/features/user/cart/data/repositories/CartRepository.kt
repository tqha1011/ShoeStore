package com.example.shoestoreapp.features.user.cart.data.repositories

import com.example.shoestoreapp.features.user.cart.data.remote.CartItemResponseDto
import com.example.shoestoreapp.features.user.cart.data.remote.CartResponseDto
import com.example.shoestoreapp.features.user.checkout.data.remote.CheckOutRequestDto

/**
 * CartRepository: Contract cho các thao tác giỏ hàng dùng ở presentation layer.
 */
interface CartRepository {
    suspend fun getCartItems(): Result<CartResponseDto>
    suspend fun addToCart(variantPublicId: String, quantity: Int): Result<CartItemResponseDto>
    suspend fun updateCartItemQuantity(cartItemId: String, quantity: Int): Result<CartItemResponseDto>
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

    companion object {
        const val ERROR_BAD_REQUEST = "The requested quantity exceeds available stock."
        const val ERROR_UNAUTHORIZED = "Your session has expired. Please sign in again."
        const val ERROR_NOT_FOUND = "The selected product variant was not found."
        const val ERROR_SERVER = "Server error. Please try again in a moment."
        const val ERROR_UNKNOWN = "Something went wrong while updating cart."
        const val FETCH_ERROR_UNAUTHORIZED = "Unauthorized. Please sign in to view your cart."
        const val FETCH_ERROR_NOT_FOUND = "User not found. Unable to load cart items."
        const val FETCH_ERROR_SERVER = "Server error while loading cart items. Please try again."
        const val FETCH_ERROR_UNKNOWN = "Unable to load cart items right now."
        const val REMOVE_ERROR_EMPTY = "No cart items provided for removal."
        const val EMPTY_RESPONSE_BODY = "Empty response body."
    }
}