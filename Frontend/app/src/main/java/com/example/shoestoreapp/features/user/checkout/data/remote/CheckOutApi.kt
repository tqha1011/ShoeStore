package com.example.shoestoreapp.features.user.checkout.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface CheckOutApi {
    @POST("/api/checkout/prepare")
    suspend fun prepareCheckout(
        @Body request: List<CheckOutRequestDto>
    ): Response<CheckOutResponseDto>

    @POST("/api/checkout/place-order")
    suspend fun placeOrder(
        @Query("fromUserCart") fromUserCart: Boolean,
        @Body request: PlaceOrderRequestDto
    ): Response<Unit>

}