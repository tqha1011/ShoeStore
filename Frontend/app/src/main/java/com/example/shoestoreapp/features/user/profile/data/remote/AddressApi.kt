package com.example.shoestoreapp.features.user.profile.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AddressApi {
    @GET("/api/address/all")
    suspend fun getAllAddresses(): Response<List<AddressResponseDto>>

    @POST("/api/address")
    suspend fun createAddress(@Body dto: CreateAddressDto): Response<Unit>

    @PUT("api/address/{addressId}")
    suspend fun updateAddress(
        @Path("addressId") addressId: String,
        @Body request: CreateAddressDto
    ): Response<Unit>

    @DELETE("api/address/{addressId}")
    suspend fun deleteAddress(
        @Path("addressId") addressId: String
    ): Response<Unit>

    @GET("api/address/{addressId}")
    suspend fun getAddressById(
        @Path("addressId") addressId: String
    ): Response<AddressResponseDto>
}
