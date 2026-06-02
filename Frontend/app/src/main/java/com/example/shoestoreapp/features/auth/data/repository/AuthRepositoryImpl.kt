    package com.example.shoestoreapp.features.auth.data.repository

    import com.example.shoestoreapp.features.auth.data.remote.AuthApi
    import com.example.shoestoreapp.features.auth.data.remote.FacebookAuthDto
    import com.example.shoestoreapp.features.auth.data.remote.GoogleAuthDto
    import com.example.shoestoreapp.features.auth.data.remote.LoginRequest
    import com.example.shoestoreapp.features.auth.data.remote.LoginResponse
    import com.example.shoestoreapp.features.auth.domain.repository.AuthRepository
    import com.example.shoestoreapp.features.auth.data.remote.RegisterRequest
    import com.example.shoestoreapp.features.auth.data.remote.UpdatePasswordRequest
    import com.example.shoestoreapp.features.auth.data.remote.VerifyEmailRequest
    import com.example.shoestoreapp.features.auth.data.remote.VerifyOtpRequest
    import kotlinx.coroutines.CancellationException
    import retrofit2.HttpException
    import java.io.IOException
    class AuthRepositoryImpl(
        // Inject AuthApi (the “steering wheel” you created earlier)
        private val api: AuthApi
    ) : AuthRepository {
        // EXTRACT LITERALS TO CONSTANTS TO PREVENT DUPLICATION
        companion object {
            private const val ERROR_OFFLINE = "You're offline. Please check your internet connection"
            private const val ERROR_UNEXPECTED = "An unexpected error occurred"
            private const val ERROR_AUTHENTICATION = "Server authentication error"
        }
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
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(Exception(ERROR_OFFLINE))
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
                Result.failure(Exception(ERROR_OFFLINE))
            }
        }

        override suspend fun loginWithGoogle(idToken: String): Result<LoginResponse> {
            return try {
                // Wrap idToken into a request
                val request = GoogleAuthDto(idToken = idToken)

                // Send request to server
                val response = api.loginWithGoogle(request)

                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Google login failed. Please try again."))
                }
            } catch (e: HttpException) {
                // Error returned from server (400, 401, ...)
                Result.failure(Exception(e.message ?: ERROR_AUTHENTICATION))
            } catch (e: IOException) {
                // Network error
                Result.failure(Exception(ERROR_OFFLINE))
            } catch (e: Exception) {
                // Other unexpected errors
                Result.failure(Exception(e.message ?: ERROR_UNEXPECTED))
            }
        }

        override suspend fun loginWithFacebook(idToken: String): Result<LoginResponse> {
            return try {
                val request = FacebookAuthDto(accessToken = idToken)
                val response = api.loginWithFacebook(request)

                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Facebook login failed. Please try again."))
                }
            } catch (e: HttpException) {
                Result.failure(Exception(e.message ?: ERROR_AUTHENTICATION))
            } catch (_: IOException) {
                Result.failure(Exception(ERROR_OFFLINE))
            } catch (e: Exception) {
                Result.failure(Exception(e.message ?: ERROR_UNEXPECTED))
            }
        }
        override suspend fun verifyEmail(email: String): Result<Unit> {
            return try {
                val response = api.verifyEmail(VerifyEmailRequest(email))
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Email not found or error occurred"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        override suspend fun verifyOtp(email: String, otp: String): Result<Unit> {
            return try {
                val response = api.verifyOtp(VerifyOtpRequest(email, otp))
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Invalid OTP. Please try again."))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        override suspend fun updatePassword(email: String, otp: String, newPassword: String): Result<Unit> {
            return try {
                val response = api.updatePassword(UpdatePasswordRequest(email, otp, newPassword))
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Failed to update password."))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }