package com.example.shoestoreapp.features.auth.presentation.reset_password.create_new_password

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

class CreateNewPasswordViewModel : ViewModel() {

    // 1. UI State Management
    private val _state = MutableStateFlow(CreateNewPasswordState())
    val state: StateFlow<CreateNewPasswordState> = _state.asStateFlow()

    // 2. One-time Event Management
    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    // 3. Handle Events
    fun onEvent(event: CreateNewPasswordEvent) {
        when (event) {
            is CreateNewPasswordEvent.PasswordChanged -> {
                _state.update { it.copy(password = event.password, passwordError = null) }
            }
            is CreateNewPasswordEvent.ConfirmPasswordChanged -> {
                _state.update { it.copy(confirmPassword = event.confirmPassword, passwordError = null) }
            }
            CreateNewPasswordEvent.TogglePasswordVisibility -> {
                _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }
            CreateNewPasswordEvent.ToggleConfirmPasswordVisibility -> {
                _state.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
            }
            CreateNewPasswordEvent.Submit -> {
                validateAndSubmit()
            }
        }
    }

    private fun validateAndSubmit() {
        val currentState = _state.value
        var hasError = false
        var passErr: String? = null
        when {
            currentState.password.length < 8 -> {
                passErr = "Password must be at least 8 characters long"
                hasError = true
            }
            !currentState.password.any { it.isUpperCase() } ||
                    !currentState.password.any { it.isLowerCase() } ||
                    !currentState.password.any { it.isDigit() } ||
                    !currentState.password.any { !it.isLetterOrDigit() } -> {
                passErr = "Password must contain at least 1 uppercase, 1 lowercase, 1 digit, and 1 special character"
                hasError = true
            }
        }

        // Validate Confirm Password (only if the main password is valid)
        if (!hasError && currentState.password != currentState.confirmPassword) {
            passErr = "Passwords do not match"
            hasError = true
        }

        // Update UI if any errors exist
        if (hasError) {
            _state.update { it.copy(passwordError = passErr) }
            return
        }

        // Proceed with mock API call
        callResetPasswordApi()
    }

    private fun callResetPasswordApi() {
        viewModelScope.launch {
            // Show loading
            _state.update { it.copy(isLoading = true) }

            // Mock API delay
            delay(1500)

            // Hide loading
            _state.update { it.copy(isLoading = false) }

            // Navigate back to Sign In
            _uiEvent.send(UiEvent.NavigateToSignIn)
        }
    }

    // UI Events
    sealed interface UiEvent {
        object NavigateToSignIn : UiEvent
        data class ShowError(val message: String) : UiEvent
    }
}