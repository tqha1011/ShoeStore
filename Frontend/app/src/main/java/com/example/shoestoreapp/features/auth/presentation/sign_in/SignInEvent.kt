package com.example.shoestoreapp.features.auth.presentation.sign_in

sealed interface SignInEvent {
    data class EmailChanged(val email: String) : SignInEvent
    data class PasswordChanged(val password: String) : SignInEvent
    object TogglePasswordVisibility : SignInEvent
    object Submit : SignInEvent
}