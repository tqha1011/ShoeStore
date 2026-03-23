package com.example.shoestoreapp.features.auth.presentation.common

sealed interface AuthUiEvent {
    object NavigateToUserHome : AuthUiEvent
    object NavigateToAdminHome : AuthUiEvent
    object NavigateToSignUp : AuthUiEvent
    object NavigateToSignIn : AuthUiEvent
    data class ShowError(val message: String) : AuthUiEvent
}