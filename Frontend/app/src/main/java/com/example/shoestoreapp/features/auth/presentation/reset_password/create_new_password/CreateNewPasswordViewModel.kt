package com.example.shoestoreapp.features.auth.presentation.reset_password.create_new_password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestoreapp.core.networks.RetrofitInstance
import com.example.shoestoreapp.features.auth.data.repository.AuthRepositoryImpl
import com.example.shoestoreapp.features.auth.domain.repository.AuthRepository
import com.example.shoestoreapp.features.auth.presentation.common.AuthUiEvent // IMPORTANT: Using the shared AuthUiEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateNewPasswordViewModel(
    private val repository: AuthRepository = AuthRepositoryImpl(RetrofitInstance.authApi)
) : ViewModel() {

    // 1. UI State Management
    private val _state = MutableStateFlow(CreateNewPasswordState())
    val state: StateFlow<CreateNewPasswordState> = _state.asStateFlow()

    // 2. Shared Event Management
    private val _uiEvent = Channel<AuthUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    // Variables to hold data passed from the Forgot Password screen
    private var targetEmail: String = ""
    private var targetOtp: String = ""

    // Initialize credentials when the screen is opened
    fun initCredentials(email: String, otp: String) {
        this.targetEmail = email
        this.targetOtp = otp
    }

    // 3. Handle User Events
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

        // Password validation logic
        when {
            currentState.password.length < 8 -> {
                passErr = "Password must be at least 8 characters long"
                hasError = true
            }
            !currentState.password.any { it.isUpperCase() } ||
                    !currentState.password.any { it.isLowerCase() } ||
                    !currentState.password.any { it.isDigit() } ||
                    !currentState.password.any { !it.isLetterOrDigit() } -> {
                passErr = "Password must contain uppercase, lowercase, digit, and special char"
                hasError = true
            }
        }

        // Confirm password matching
        if (!hasError && currentState.password != currentState.confirmPassword) {
            passErr = "Passwords do not match"
            hasError = true
        }

        // Update state if validation fails
        if (hasError) {
            _state.update { it.copy(passwordError = passErr) }
            return
        }

        callResetPasswordApi()
    }

    private fun callResetPasswordApi() {
        viewModelScope.launch {
            // Turn on loading spinner
            _state.update { it.copy(isLoading = true) }

            // Call the API to update the password using the passed email and otp
            val result = repository.updatePassword(
                email = targetEmail,
                otp = targetOtp,
                newPassword = _state.value.password
            )

            result.fold(
                onSuccess = {
                    _state.update { it.copy(isLoading = false) }
                    // Navigate back to Sign In screen upon success
                    _uiEvent.trySend(AuthUiEvent.NavigateToSignIn)
                },
                onFailure = { error ->
                    _state.update { it.copy(
                        isLoading = false,
                        passwordError = error.message ?: "Failed to update password. Please try again."
                    )}
                }
            )
        }
    }
}