package com.example.shoestoreapp.features.user.checkout.data.repositories

import com.example.shoestoreapp.core.networks.RetrofitInstance
import com.example.shoestoreapp.features.user.checkout.data.remote.CheckOutApi
import com.example.shoestoreapp.features.user.checkout.data.remote.CheckOutResponseDto
import com.example.shoestoreapp.features.user.checkout.data.remote.InvoiceDto
import com.example.shoestoreapp.features.user.checkout.data.remote.PlaceOrderRequestDto
import com.example.shoestoreapp.features.user.checkout.data.remote.PrepareCheckOutRequestDto
import retrofit2.HttpException

interface CheckOutRepository {
    suspend fun prepareCheckOut(request: PrepareCheckOutRequestDto): Result<CheckOutResponseDto>
    suspend fun placeOrder(fromUserCart: Boolean, request: PlaceOrderRequestDto): Result<InvoiceDto>
}

class CheckoutRepositoryImpl(
    private val checkOutApi: CheckOutApi = RetrofitInstance.checkOutApi
) : CheckOutRepository {

    override suspend fun prepareCheckOut(request: PrepareCheckOutRequestDto): Result<CheckOutResponseDto> {
        return try {
            val response = checkOutApi.prepareCheckout(request)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun placeOrder(fromUserCart: Boolean, request: PlaceOrderRequestDto): Result<InvoiceDto> {
        return try {
            val response = checkOutApi.placeOrder(fromUserCart, request)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty invoice data"))
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}