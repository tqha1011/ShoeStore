package com.example.shoestoreapp.features.auth.presentation.sign_up

import android.util.Patterns
import androidx.lifecycle.viewModelScope
import com.example.shoestoreapp.core.networks.RetrofitInstance
import com.example.shoestoreapp.core.utils.TokenManager
import com.example.shoestoreapp.features.auth.data.remote.RegisterRequest
import com.example.shoestoreapp.features.auth.data.repository.AuthRepositoryImpl
import com.example.shoestoreapp.features.auth.domain.repository.AuthRepository
import com.example.shoestoreapp.features.auth.presentation.common.BaseAuthViewModel // Nhớ check lại đường dẫn
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SignUpViewModel(
    repository: AuthRepository = AuthRepositoryImpl(RetrofitInstance.authApi),
    tokenManager: TokenManager
) : BaseAuthViewModel(repository, tokenManager) {

    // 1. UI State Management
    private val _state = MutableStateFlow(SignUpState())
    val state: StateFlow<SignUpState> = _state.asStateFlow()

    // 2. One-time Event Management
    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    // --- Implement BaseAuthViewModel abstract methods ---

    override fun updateLoadingState(isLoading: Boolean) {
        _state.update { it.copy(isLoading = isLoading) }
    }

    override suspend fun onSocialLoginSuccess(role: String) {
        _uiEvent.send(UiEvent.NavigateToUserHome)
    }

    override suspend fun onSocialLoginFailure(errorMessage: String) {
        _state.update { it.copy(passwordError = errorMessage) }
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
                passErr =
                    "Password must contain at least 1 uppercase, 1 lowercase, 1 digit, and 1 special character"
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

            // 1. Turn on loading spinner
            _state.update { it.copy(isLoading = true) }

            // 2. Package data into DTO to send to server
            val request = RegisterRequest(
                email = currentState.email,
                password = currentState.password,
                confirmPassword = currentState.confirmPassword
            )

            // 3. Repository uses Retrofit to send request to server
            val result = repository.register(request)

            // 4. Unwrap Result to check success or failure
            result.fold(
                onSuccess = {
                    _state.update { it.copy(isLoading = false) }
                    _uiEvent.send(UiEvent.NavigateToUserHome)
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            passwordError = error.message ?: "Registration failed. Please try again!"
                        )
                    }
                }
            )
        }
    }

    // Defines UiEvent
    sealed interface UiEvent {
        object NavigateToSignIn : UiEvent
        object NavigateToUserHome : UiEvent
        data class ShowError(val message: String) : UiEvent
    }
}