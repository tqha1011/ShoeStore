package com.example.shoestoreapp.features.auth.presentation.components

sealed interface AuthUiEvent {
    object NavigateToUserHome : AuthUiEvent
    object NavigateToAdminHome : AuthUiEvent
    object NavigateToSignUp : AuthUiEvent
    object NavigateToSignIn : AuthUiEvent
    data class ShowError(val message: String) : AuthUiEvent
    data class NavigateToResetPassword(val email: String) : AuthUiEvent
    data class NavigateToCreateNewPassword(val email: String, val otp: String) : AuthUiEvent

}