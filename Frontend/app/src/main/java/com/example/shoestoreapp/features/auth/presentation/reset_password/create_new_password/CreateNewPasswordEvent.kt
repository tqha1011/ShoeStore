package com.example.shoestoreapp.features.auth.presentation.reset_password.create_new_password

sealed interface CreateNewPasswordEvent {
    data class PasswordChanged(val password: String) : CreateNewPasswordEvent
    data class ConfirmPasswordChanged(val confirmPassword: String) : CreateNewPasswordEvent
    object TogglePasswordVisibility : CreateNewPasswordEvent
    object ToggleConfirmPasswordVisibility : CreateNewPasswordEvent
    object Submit : CreateNewPasswordEvent
}
