package com.example.shoestoreapp.features.auth.presentation.reset_password.forgot_password

sealed interface ForgotPasswordEvent {
    data class EmailChanged(val email: String) : ForgotPasswordEvent
    data class VerificationCodeChanged(val code: String) : ForgotPasswordEvent
    object SubmitEmail : ForgotPasswordEvent // First Click
    object SubmitCode : ForgotPasswordEvent  // Second Click
}