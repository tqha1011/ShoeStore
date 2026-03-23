package com.example.shoestoreapp.features.auth.presentation.sign_up

import android.util.Patterns
import androidx.lifecycle.viewModelScope
import com.example.shoestoreapp.core.networks.RetrofitInstance
import com.example.shoestoreapp.core.utils.TokenManager
import com.example.shoestoreapp.features.auth.data.remote.RegisterRequest
import com.example.shoestoreapp.features.auth.data.repository.AuthRepositoryImpl
import com.example.shoestoreapp.features.auth.domain.repository.AuthRepository
import com.example.shoestoreapp.features.auth.presentation.common.AuthUiEvent
import com.example.shoestoreapp.features.auth.presentation.common.BaseAuthViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SignUpViewModel(
    repository: AuthRepository = AuthRepositoryImpl(RetrofitInstance.authApi),
    tokenManager: TokenManager
) : BaseAuthViewModel<SignUpState>(repository, tokenManager, SignUpState()) {

    // 1. Using common AuthUiEvent to fix duplication
    private val _uiEvent = Channel<AuthUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    // --- Implement abstract methods from BaseAuthViewModel ---

    override fun updateLoading(isLoading: Boolean) {
        _state.update { it.copy(isLoading = isLoading) }
    }

    override suspend fun handleSocialSuccess(role: String) {
        // Social login users always go to User Home
        _uiEvent.send(AuthUiEvent.NavigateToUserHome)
    }

    override fun handleSocialError(message: String) {
        _state.update { it.copy(passwordError = message) }
    }

    // --- Standard Register Logic ---

    fun onEvent(event: SignUpEvent) {
        when (event) {
            is SignUpEvent.EmailChanged -> {
                _state.update { it.copy(email = event.email, emailError = null) }
            }
            is SignUpEvent.PasswordChanged -> {
                _state.update { it.copy(password = event.password, passwordError = null) }
            }
            is SignUpEvent.ConfirmPasswordChanged -> {
                _state.update {
                    it.copy(
                        confirmPassword = event.confirmPassword,
                        confirmPasswordError = null
                    )
                }
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
                passErr = "Password must contain uppercase, lowercase, digit, and special char"
                hasError = true
            }
        }

        // Validate Confirm Password
        if (currentState.password != currentState.confirmPassword) {
            confirmPassErr = "Passwords do not match"
            hasError = true
        }

        if (hasError) {
            _state.update {
                it.copy(
                    emailError = emailErr,
                    passwordError = passErr,
                    confirmPasswordError = confirmPassErr
                )
            }
            return
        }

        callSignUpApi()
    }

    private fun callSignUpApi() {
        viewModelScope.launch {
            val currentState = _state.value
            _state.update { it.copy(isLoading = true) }

            val request = RegisterRequest(
                email = currentState.email,
                password = currentState.password,
                confirmPassword = currentState.confirmPassword
            )

            val result = repository.register(request)
            _state.update { it.copy(isLoading = false) }

            result.fold(
                onSuccess = {
                    _uiEvent.send(AuthUiEvent.NavigateToUserHome)
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            passwordError = error.message ?: "Registration failed!"
                        )
                    }
                }
            )
        }
    }
}