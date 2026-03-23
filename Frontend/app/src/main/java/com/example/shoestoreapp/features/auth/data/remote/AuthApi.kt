package com.example.shoestoreapp.features.auth.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("api/Auth/signin")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @POST("api/Auth/signup")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<RegisterResponse>

    @POST("api/auth/signin-google")
    suspend fun loginWithGoogle(
        @Body request: GoogleAuthDto
    ): Response<LoginResponse>

    @POST("api/auth/signin-facebook")
    suspend fun loginWithFacebook(
        @Body request: FacebookAuthDto
    ): Response<LoginResponse>
}