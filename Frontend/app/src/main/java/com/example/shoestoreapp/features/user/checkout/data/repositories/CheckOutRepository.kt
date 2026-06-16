package com.example.shoestoreapp.features.user.checkout.data.repositories

import com.example.shoestoreapp.features.user.checkout.data.remote.CheckOutResponseDto
import com.example.shoestoreapp.features.user.checkout.data.remote.InvoiceDto
import com.example.shoestoreapp.features.user.checkout.data.remote.PaymentStatusResponse
import com.example.shoestoreapp.features.user.checkout.data.remote.PlaceOrderRequestDto
import com.example.shoestoreapp.features.user.checkout.data.remote.PrepareCheckOutRequestDto

interface CheckOutRepository {
    suspend fun prepareCheckOut(request: PrepareCheckOutRequestDto): Result<CheckOutResponseDto>
    suspend fun placeOrder(fromUserCart: Boolean, request: PlaceOrderRequestDto): Result<InvoiceDto>
    suspend fun checkPaymentStatus(orderCode: String): Result<PaymentStatusResponse>
}

sealed class CheckOutRepositoryException(message: String) : Exception(message) {
    class BadRequest(message: String = ERROR_BAD_REQUEST) : CheckOutRepositoryException(message)
    class Unauthorized(message: String = ERROR_UNAUTHORIZED) : CheckOutRepositoryException(message)
    class NotFound(message: String = ERROR_NOT_FOUND) : CheckOutRepositoryException(message)
    class ServerError(message: String = ERROR_SERVER) : CheckOutRepositoryException(message)
    class Unknown(message: String = ERROR_UNKNOWN) : CheckOutRepositoryException(message)

    companion object {
        const val ERROR_BAD_REQUEST = "Invalid checkout data. Please check your information."
        const val ERROR_UNAUTHORIZED = "Unauthorized. Please sign in again."
        const val ERROR_NOT_FOUND = "Resource not found."
        const val ERROR_SERVER = "Server error. Please try again later."
        const val ERROR_UNKNOWN = "Something went wrong during checkout."
        const val EMPTY_RESPONSE_BODY = "Empty response body"
    }
}