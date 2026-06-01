package com.example.shoestoreapp.features.user.profile.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AddressApi {
    @GET("/api/address/all")
    suspend fun getAllAddresses(): Response<List<AddressResponseDto>>

    @POST("/api/address")
    suspend fun createAddress(@Body dto: CreateAddressDto): Response<Unit>
}
