package com.example.shoestoreapp.features.auth.presentation.reset_password.forgot_password

// Holds UI data only
data class ForgotPasswordState(
    val email: String = "",
    val verificationCode: String = "",
    val isCodeSent: Boolean = false, // Check if verification code has been sent
    val emailError: String? = null,
    val verificationError: String? = null,
    val isLoading: Boolean = false
)