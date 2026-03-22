package com.example.shoestoreapp.features.auth.data.remote

import com.example.shoestoreapp.features.auth.data.remote.LoginRequest
import com.example.shoestoreapp.features.auth.data.remote.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    // Định nghĩa API Đăng nhập
    // Chữ "login" ở dưới phụ thuộc vào Backend của m. (ví dụ BASE_URL/login)
    @POST("api/Auth/signin")
    suspend fun login(
        @Body request: LoginRequest // Gửi đi cục LoginRequest (email, pass)
    ): Response<LoginResponse>      // Hứng về cục LoginResponse (token, user_id)

    @POST("api/Auth/signup")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<RegisterResponse>
}