package com.example.shoestoreapp.features.user.checkout.data.repositories

import com.example.shoestoreapp.core.networks.RetrofitInstance
import com.example.shoestoreapp.features.user.checkout.data.remote.CheckOutApi
import com.example.shoestoreapp.features.user.checkout.data.remote.CheckOutRequestDto
import com.example.shoestoreapp.features.user.checkout.data.remote.CheckOutResponseDto
import com.example.shoestoreapp.features.user.checkout.data.remote.PlaceOrderRequestDto

interface CheckOutRepository {
    /**
     * Chuẩn bị dữ liệu checkout để gửi lên server
     */
    suspend fun prepareCheckOut(request: List<CheckOutRequestDto>): Result<CheckOutResponseDto>

    /**
     * Gửi yêu cầu đặt hàng đến server
     */
    suspend fun placeOrder(fromUserCart: Boolean, request: PlaceOrderRequestDto): Result<Unit>
}

class CheckoutRepositoryImpl(
    private val checkOutApi: CheckOutApi = RetrofitInstance.checkOutApi
) : CheckOutRepository {

    override suspend fun prepareCheckOut(request: List<CheckOutRequestDto>): Result<CheckOutResponseDto> {
        return try {
            val response = checkOutApi.prepareCheckout(request)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "Unauthorized: user must be logged in with a valid JWT token."
                    404 -> "Not Found: one or more product variants do not exist."
                    421 -> "Too many requests: rate limit exceeded for this user."
                    500 -> "Internal server error: an unexpected server error occurred."
                    else -> "Unknown error: ${response.code()}"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (_ : Exception) {
            Result.failure(Exception("Unknown error"))
        }
    }

    override suspend fun placeOrder(fromUserCart: Boolean, request: PlaceOrderRequestDto): Result<Unit> {
        return try {
            val response = checkOutApi.placeOrder(fromUserCart, request)

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "Bad Request"
                    401 -> "Unauthorized: user must be logged in with a valid JWT token."
                    404 -> "Not Found: one or more product variants do not exist."
                    409 -> "Conflict: the order already exists."
                    429 -> "Too many requests: rate limit exceeded for this user."
                    500 -> "Internal server error: an unexpected server error occurred."
                    else -> "Unknown error: ${response.code()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Unknown error"))
        }
    }
}
