package com.example.shoestoreapp.features.auth.presentation.sign_up_verify

sealed interface SignUpOtpEvent {
    data class OtpChanged(val otp: String) : SignUpOtpEvent
    data object Submit : SignUpOtpEvent
}
