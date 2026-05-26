package com.example.shoestoreapp.features.user.profile.data.remote

import retrofit2.Response
import retrofit2.http.GET

interface ProfileApi {
    @GET("/api/profile")
    suspend fun getUserProfile(): Response<ResponseProfileDto>
}

