package com.example.shoestoreapp.features.auth.domain.repository

import com.example.shoestoreapp.features.auth.data.remote.LoginRequest
import com.example.shoestoreapp.features.auth.data.remote.LoginResponse
import com.example.shoestoreapp.features.auth.data.remote.RegisterRequest
import com.example.shoestoreapp.features.auth.data.remote.RegisterResponse

interface AuthRepository {
    // Hàm gọi đăng nhập.
    // Dùng Result<LoginResponse> để bọc kết quả lại (chứa trạng thái Success hoặc Failure)
    suspend fun login(request: LoginRequest): Result<LoginResponse>

    suspend fun register(request: RegisterRequest): Result<String>
}