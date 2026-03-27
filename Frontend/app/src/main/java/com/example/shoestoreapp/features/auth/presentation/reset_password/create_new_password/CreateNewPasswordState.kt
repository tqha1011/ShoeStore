package com.example.shoestoreapp.features.auth.presentation.reset_password.create_new_password

data class CreateNewPasswordState(
    val password: String = "",
    val confirmPassword: String = "",
    val passwordError: String? = null,
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false
)