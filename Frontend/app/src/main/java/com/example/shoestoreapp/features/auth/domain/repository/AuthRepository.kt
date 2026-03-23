package com.example.shoestoreapp.features.auth.domain.repository

import com.example.shoestoreapp.features.auth.data.remote.LoginRequest
import com.example.shoestoreapp.features.auth.data.remote.LoginResponse
import com.example.shoestoreapp.features.auth.data.remote.RegisterRequest
import com.example.shoestoreapp.features.auth.data.remote.RegisterResponse

interface AuthRepository {
    suspend fun login(request: LoginRequest): Result<LoginResponse>

    suspend fun register(request: RegisterRequest): Result<String>
    suspend fun loginWithGoogle(idToken: String) : Result<LoginResponse>
    suspend fun loginWithFacebook(idToken: String): Result<LoginResponse>
}