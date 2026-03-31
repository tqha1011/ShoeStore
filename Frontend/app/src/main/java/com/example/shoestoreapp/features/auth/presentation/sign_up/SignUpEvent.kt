package com.example.shoestoreapp.features.auth.presentation.sign_up

sealed interface SignUpEvent {
    data class EmailChanged(val email: String) : SignUpEvent
    data class PasswordChanged(val password: String) : SignUpEvent
    data class ConfirmPasswordChanged(val confirmPassword: String) : SignUpEvent
    object TogglePasswordVisibility : SignUpEvent
    object ToggleConfirmPasswordVisibility : SignUpEvent
    object Submit : SignUpEvent
}