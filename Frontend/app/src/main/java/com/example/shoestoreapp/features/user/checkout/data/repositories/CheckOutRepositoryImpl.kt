package com.example.shoestoreapp.features.user.checkout.data.repositories

import com.example.shoestoreapp.core.networks.RetrofitInstance
import com.example.shoestoreapp.features.user.checkout.data.remote.CheckOutApi
import com.example.shoestoreapp.features.user.checkout.data.remote.CheckOutResponseDto
import com.example.shoestoreapp.features.user.checkout.data.remote.InvoiceDto
import com.example.shoestoreapp.features.user.checkout.data.remote.PlaceOrderRequestDto
import com.example.shoestoreapp.features.user.checkout.data.remote.PrepareCheckOutRequestDto
import org.json.JSONObject
import retrofit2.Response

class CheckoutRepositoryImpl(
    private val checkOutApi: CheckOutApi = RetrofitInstance.checkOutApi
) : CheckOutRepository {

    override suspend fun prepareCheckOut(request: PrepareCheckOutRequestDto): Result<CheckOutResponseDto> {
        return try {
            val response = checkOutApi.prepareCheckout(request)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(CheckOutRepositoryException.Unknown(CheckOutRepositoryException.EMPTY_RESPONSE_BODY))
            } else {
                Result.failure(response.toRepositoryException())
            }
        } catch (e: Exception) {
            Result.failure(CheckOutRepositoryException.Unknown(e.message ?: CheckOutRepositoryException.ERROR_UNKNOWN))
        }
    }

    override suspend fun placeOrder(fromUserCart: Boolean, request: PlaceOrderRequestDto): Result<InvoiceDto> {
        return try {
            val response = checkOutApi.placeOrder(fromUserCart, request)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(CheckOutRepositoryException.Unknown("Empty invoice data"))
            } else {
                Result.failure(response.toRepositoryException())
            }
        } catch (e: Exception) {
            Result.failure(CheckOutRepositoryException.Unknown(e.message ?: CheckOutRepositoryException.ERROR_UNKNOWN))
        }
    }

    // ==================
    // LOGIC PARSE LỖI
    // ==================

    private fun <T> Response<T>.toRepositoryException(): CheckOutRepositoryException {
        val rawMessage = errorBody()?.string()?.takeIf { it.isNotBlank() }
        val backendMessage = parseBackendError(rawMessage)

        return when (code()) {
            400 -> CheckOutRepositoryException.BadRequest(backendMessage ?: CheckOutRepositoryException.ERROR_BAD_REQUEST)
            401 -> CheckOutRepositoryException.Unauthorized(backendMessage ?: CheckOutRepositoryException.ERROR_UNAUTHORIZED)
            404 -> CheckOutRepositoryException.NotFound(backendMessage ?: CheckOutRepositoryException.ERROR_NOT_FOUND)
            500 -> CheckOutRepositoryException.ServerError(backendMessage ?: CheckOutRepositoryException.ERROR_SERVER)
            else -> CheckOutRepositoryException.Unknown(
                backendMessage ?: "${CheckOutRepositoryException.ERROR_UNKNOWN} (HTTP ${code()})"
            )
        }
    }

    private fun parseBackendError(rawMessage: String?): String? {
        if (rawMessage.isNullOrBlank()) return null

        return try {
            val jsonObject = JSONObject(rawMessage)

            if (jsonObject.has("errors")) {
                val errorsObj = jsonObject.getJSONObject("errors")
                val errorMessages = mutableListOf<String>()
                val keys = errorsObj.keys()

                while (keys.hasNext()) {
                    val key = keys.next()
                    val errorArray = errorsObj.getJSONArray(key)
                    for (i in 0 until errorArray.length()) {
                        errorMessages.add(errorArray.getString(i))
                    }
                }

                if (errorMessages.isNotEmpty()) {
                    return errorMessages.joinToString("\n")
                }
            }

            if (jsonObject.has("title")) {
                return jsonObject.getString("title")
            }

            rawMessage
        } catch (_: Exception) {
            rawMessage
        }
    }
}