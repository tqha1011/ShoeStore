package com.example.shoestoreapp.features.auth.presentation.sign_up_verify

data class SignUpOtpState(
    val otpCode: String = "",
    val isLoading: Boolean = false,
    val otpError: String? = null,
    val successMessage: String? = null
)
