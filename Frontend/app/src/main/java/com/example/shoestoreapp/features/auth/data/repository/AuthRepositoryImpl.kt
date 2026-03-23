package com.example.shoestoreapp.features.auth.data.repository

import com.example.shoestoreapp.features.auth.data.remote.AuthApi
import com.example.shoestoreapp.features.auth.data.remote.LoginRequest
import com.example.shoestoreapp.features.auth.data.remote.LoginResponse
import com.example.shoestoreapp.features.auth.domain.repository.AuthRepository
import com.example.shoestoreapp.features.auth.data.remote.RegisterRequest
import com.example.shoestoreapp.features.auth.data.remote.GoogleLoginRequest
import retrofit2.HttpException
import java.io.IOException
class AuthRepositoryImpl(
    // Inject AuthApi (the “steering wheel” you created earlier)
    private val api: AuthApi
) : AuthRepository {

    override suspend fun login(request: LoginRequest): Result<LoginResponse> {
        return try {
            val response = api.login(request)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                // Error code list
                val errorMessage = when (response.code()) {
                    400 -> "Invalid request format"
                    401, 404 -> "Invalid email or password!"
                    429 -> "Too many requests. Please try again later!"
                    500, 502, 503 -> "Server is under maintenance. Please try again later!"
                    else -> "Login failed. Please try again!"
                }
                Result.failure(Exception(errorMessage))
            }
            // No internet connection
        } catch (e: Exception) {
            Result.failure(Exception("You're offline. Please check your internet connection"))
        }
    }

    override suspend fun register(request: RegisterRequest): Result<String> {
        return try {
            val response = api.register(request)

            if (response.isSuccessful && response.body() != null) {
                // Registration successful -> Return message from server
                Result.success(response.body()?.message ?: "Account registered successfully!")
            } else {
                // Error (e.g., 409 = email already exists)
                val errorMessage = when (response.code()) {
                    409 -> "This email has already been used!"
                    500, 502, 503 -> "Server is under maintenance. Please try again later!"
                    else -> "Login failed. Please try again!"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("You're offline. Please check your internet connection"))
        }
    }

    override suspend fun loginWithGoogle(idToken: String): Result<LoginResponse> {
        return try {
            // Wrap idToken into a request
            val request = GoogleLoginRequest(idToken = idToken)

            // Send request to server
            val response = api.loginWithGoogle(request)

            Result.success(response)
        } catch (e: HttpException) {
            // Error returned from server (400, 401, ...)
            Result.failure(Exception(e.message ?: "Server authentication error"))
        } catch (e: IOException) {
            // Network error
            Result.failure(Exception("You're offline. Please check your internet connection"))
        } catch (e: Exception) {
            // Other unexpected errors
            Result.failure(Exception(e.message ?: "An unexpected error occurred"))
        }
    }
}