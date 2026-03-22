package com.example.shoestoreapp.features.auth.presentation.reset_password.forgot_password

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ForgotPasswordViewModel : ViewModel() {

    // 1. UI State Management
    private val _state = MutableStateFlow(ForgotPasswordState())
    val state: StateFlow<ForgotPasswordState> = _state.asStateFlow()

    // 2. One-time Event Management
    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    // 3. Handle Events
    fun onEvent(event: ForgotPasswordEvent) {
        when (event) {
            is ForgotPasswordEvent.EmailChanged -> {
                _state.update { it.copy(email = event.email, emailError = null) }
            }
            is ForgotPasswordEvent.VerificationCodeChanged -> {
                _state.update { it.copy(verificationCode = event.code, verificationError = null) }
            }
            ForgotPasswordEvent.SubmitEmail -> {
                sendVerificationCode()
            }
            ForgotPasswordEvent.SubmitCode -> {
                verifyCode()
            }
        }
    }

    private fun sendVerificationCode() {
        val currentState = _state.value
        var hasError = false
        var emailErr: String? = null

        if (currentState.email.isBlank()) {
            emailErr = "Please enter your email"
            hasError = true
        } else if (!Patterns.EMAIL_ADDRESS.matcher(currentState.email).matches()) {
            emailErr = "Invalid email format"
            hasError = true
        }

        if (hasError) {
            _state.update { it.copy(emailError = emailErr) }
            return
        }

        viewModelScope.launch {
            // Show loading
            _state.update { it.copy(isLoading = true) }

            // Mock API call to send email
            delay(1500)

            // Hide loading and show the verification code input field
            _state.update { it.copy(
                isLoading = false,
                isCodeSent = true
            )}
        }
    }

    private fun verifyCode() {
        val currentState = _state.value

        if (currentState.verificationCode.isBlank()) {
            _state.update { it.copy(verificationError = "Please enter verification code") }
            return
        }

        viewModelScope.launch {
            // Show loading
            _state.update { it.copy(isLoading = true) }

            // Mock API call to verify code
            delay(1500)

            // Hide loading
            _state.update { it.copy(isLoading = false) }

            // Trigger navigation
            _uiEvent.send(UiEvent.NavigateToCreateNewPassword)
        }
    }

    // UI Events
    sealed interface UiEvent {
        object NavigateToCreateNewPassword : UiEvent
        data class ShowError(val message: String) : UiEvent
    }
}