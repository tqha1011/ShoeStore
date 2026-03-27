package com.example.shoestoreapp.features.auth.data.remote

data class VerifyEmailRequest(
    val email: String
)

data class VerifyOtpRequest(
    val email: String,
    val otp: String
)

data class UpdatePasswordRequest(
    val email: String,
    val otp: String,
    val newPassword: String
)