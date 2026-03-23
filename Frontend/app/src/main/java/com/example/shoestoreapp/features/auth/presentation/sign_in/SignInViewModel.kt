package com.example.shoestoreapp.features.auth.presentation.sign_in

import android.util.Patterns
import androidx.lifecycle.viewModelScope
import com.example.shoestoreapp.core.networks.RetrofitInstance
import com.example.shoestoreapp.core.utils.JwtUtils
import com.example.shoestoreapp.core.utils.TokenManager
import com.example.shoestoreapp.features.auth.data.remote.LoginRequest
import com.example.shoestoreapp.features.auth.data.repository.AuthRepositoryImpl
import com.example.shoestoreapp.features.auth.domain.repository.AuthRepository
import com.example.shoestoreapp.features.auth.presentation.common.AuthUiEvent
import com.example.shoestoreapp.features.auth.presentation.common.BaseAuthViewModel // Nhớ check lại đường dẫn package chỗ này nhé
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SignInViewModel(
    repository: AuthRepository = AuthRepositoryImpl(RetrofitInstance.authApi),
    tokenManager: TokenManager
) : BaseAuthViewModel<SignInState>(repository, tokenManager, SignInState()) {

    private val _uiEvent = Channel<AuthUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    override fun updateLoading(isLoading: Boolean) {
        _state.update { it.copy(isLoading = isLoading) }
    }

    override suspend fun handleSocialSuccess(role: String) {
        _uiEvent.send(AuthUiEvent.NavigateToUserHome)
    }

    override fun handleSocialError(message: String) {
        _state.update { it.copy(passwordError = message) }
    }

    // --- Standard Login Logic ---

    fun onEvent(event: SignInEvent) {
        when (event) {
            is SignInEvent.EmailChanged -> {
                _state.update { it.copy(email = event.email, emailError = null) }
            }

            is SignInEvent.PasswordChanged -> {
                _state.update { it.copy(password = event.password, passwordError = null) }
            }

            SignInEvent.TogglePasswordVisibility -> {
                _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }

            SignInEvent.Submit -> {
                validateAndSubmit()
            }
        }
    }

    private fun validateAndSubmit() {
        val currentState = _state.value
        var hasError = false
        var emailErr: String? = null
        var passErr: String? = null

        if (currentState.email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(currentState.email)
                .matches()
        ) {
            emailErr = "Invalid email format"
            hasError = true
        }
        if (currentState.password.isEmpty()) {
            passErr = "Password cannot be empty"
            hasError = true
        }
        if (hasError) {
            _state.update {
                it.copy(
                    emailError = emailErr,
                    passwordError = passErr,
                    email = if (emailErr != null) "" else it.email,
                    password = if (passErr != null) "" else it.password
                )
            }
            return
        }

        // Validate OK -> Call API
        callSignInApi()
    }

    private fun callSignInApi() {
        viewModelScope.launch {
            val currentState = _state.value

            // 1. Show loading
            _state.update { it.copy(isLoading = true) }

            // 2. Prepare DTO
            val request = LoginRequest(
                email = currentState.email,
                password = currentState.password
            )

            // 3. Call API via Repository
            val result = repository.login(request)

            // 4. Hide loading
            _state.update { it.copy(isLoading = false) }

            // 5. Handle Result
            result.fold(
                onSuccess = { response ->
                    val token = response.token
                    val role = JwtUtils.getRoleFromToken(token)

                    // Save Token and Role to DataStore
                    tokenManager.saveAuthInfo(token = token, role = role)

                    // Navigate based on Role for standard login
                    if (role.uppercase() == "ADMIN") {
                        _uiEvent.send(AuthUiEvent.NavigateToAdminHome)
                    } else {
                        _uiEvent.send(AuthUiEvent.NavigateToUserHome)
                    }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            emailError = error.message,
                            passwordError = error.message,
                            email = "",
                            password = "",
                        )
                    }
                }
            )
        }
    }
}