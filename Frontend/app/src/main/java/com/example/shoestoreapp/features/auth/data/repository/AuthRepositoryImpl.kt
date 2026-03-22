package com.example.shoestoreapp.features.auth.data.repository

import com.example.shoestoreapp.features.auth.data.remote.AuthApi
import com.example.shoestoreapp.features.auth.data.remote.LoginRequest
import com.example.shoestoreapp.features.auth.data.remote.LoginResponse
import com.example.shoestoreapp.features.auth.domain.repository.AuthRepository
import com.example.shoestoreapp.features.auth.data.remote.RegisterRequest
import com.example.shoestoreapp.features.auth.data.remote.RegisterResponse
class AuthRepositoryImpl(
    // Truyền cái AuthApi (chiếc vô lăng m vừa tạo) vào đây
    private val api: AuthApi
) : AuthRepository {

    override suspend fun login(request: LoginRequest): Result<LoginResponse> {
        return try {
            val response = api.login(request)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                // --- SỬA Ở ĐÂY NÀY ---
                // Tùy theo mã lỗi Server trả về mà mình dịch ra tiếng Việt cho thân thiện
                val errorMessage = when (response.code()) {
                    401, 404 -> "Invalid email or password!"
                    500, 502, 503 -> "Server is under maintenance. Please try again later!"
                    else -> "Login failed. Please try again!"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            // Lỗi này rớt vào khi đứt mạng, tắt wifi...
            Result.failure(Exception("Không có kết nối mạng. Hãy kiểm tra lại Wifi/4G!"))
        }
    }
    override suspend fun register(request: RegisterRequest): Result<String> {
        return try {
            val response = api.register(request)

            if (response.isSuccessful && response.body() != null) {
                // Đăng ký thành công -> Trả về câu thông báo của Server
                Result.success(response.body()?.message ?: "Đăng ký tài khoản thành công!")
            } else {
                // Lỗi (Ví dụ: 400 là Email đã tồn tại)
                val errorMessage = when (response.code()) {
                    400 -> "Email này đã được sử dụng!"
                    500 -> "Lỗi Server, vui lòng thử lại sau!"
                    else -> "Đăng ký thất bại. Lỗi: ${response.code()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Không có kết nối mạng. Hãy kiểm tra lại Wifi/4G!"))
        }
    }
}