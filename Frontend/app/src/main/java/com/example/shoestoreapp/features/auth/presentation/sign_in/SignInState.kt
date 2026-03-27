package com.example.shoestoreapp.features.auth.presentation.sign_in


data class SignInState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false // Loading call api
)