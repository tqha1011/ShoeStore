package com.example.shoestoreapp.features.user.profile.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Body

interface ProfileApi {
    @GET("/api/profile")
    suspend fun getUserProfile(): Response<ResponseProfileDto>

    @PUT("/api/profile")
    suspend fun updateProfile(@Body request: UpdateProfileDto): Response<Unit>

    @PUT("/change-password")
    suspend fun changePassword(@Body request: ChangePasswordDto): Response<Unit>
}