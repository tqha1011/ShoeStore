package com.example.shoestoreapp.features.auth.presentation.sign_up

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

class SignUpViewModel : ViewModel() {

    // 1. UI State Management
    private val _state = MutableStateFlow(SignUpState())
    val state: StateFlow<SignUpState> = _state.asStateFlow()

    // 2. One-time Event Management
    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    // 3. Single entry point for all UI events
    fun onEvent(event: SignUpEvent) {
        when (event) {
            is SignUpEvent.EmailChanged -> {
                _state.update { it.copy(email = event.email, emailError = null) }
            }
            is SignUpEvent.PasswordChanged -> {
                _state.update { it.copy(password = event.password, passwordError = null) }
            }
            is SignUpEvent.ConfirmPasswordChanged -> {
                _state.update { it.copy(confirmPassword = event.confirmPassword, confirmPasswordError = null) }
            }
            SignUpEvent.TogglePasswordVisibility -> {
                _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }
            SignUpEvent.ToggleConfirmPasswordVisibility -> {
                _state.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
            }
            SignUpEvent.Submit -> {
                validateAndSubmit()
            }
        }
    }

    private fun validateAndSubmit() {
        val currentState = _state.value
        var hasError = false
        var emailErr: String? = null
        var passErr: String? = null
        var confirmPassErr: String? = null

        // Validate Email
        if (!Patterns.EMAIL_ADDRESS.matcher(currentState.email).matches()) {
            emailErr = "Invalid email format"
            hasError = true
        }

        // Validate Password Complexity
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

        // Validate Confirm Password
        if (currentState.password != currentState.confirmPassword) {
            confirmPassErr = "Passwords do not match"
            hasError = true
        }

        // Update UI if any errors exist
        if (hasError) {
            _state.update { it.copy(
                emailError = emailErr,
                passwordError = passErr,
                confirmPasswordError = confirmPassErr
            )}
            return
        }

        callSignUpApi()
    }

    private fun callSignUpApi() {
        viewModelScope.launch {
            // Show loading spinner
            _state.update { it.copy(isLoading = true) }

            // Mock API call delay
            delay(1500)

            // Hide loading spinner
            _state.update { it.copy(isLoading = false) }

            // Navigate back to Sign In upon success
            _uiEvent.send(UiEvent.NavigateToSignIn)
        }
    }

    // Defines UiEvent
    sealed interface UiEvent {
        object NavigateToSignIn : UiEvent
        data class ShowError(val message: String) : UiEvent
    }
}