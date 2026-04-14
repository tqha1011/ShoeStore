package com.example.shoestoreapp.features.auth.presentation.reset_password.forgot_password

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestoreapp.core.networks.RetrofitInstance
import com.example.shoestoreapp.features.auth.data.repository.AuthRepositoryImpl
import com.example.shoestoreapp.features.auth.domain.repository.AuthRepository
import com.example.shoestoreapp.features.auth.presentation.components.AuthUiEvent // Dùng hàng chung
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    private val repository: AuthRepository = AuthRepositoryImpl(RetrofitInstance.authApi)
) : ViewModel() {

    // 1. UI State Management
    private val _state = MutableStateFlow(ForgotPasswordState())
    val state: StateFlow<ForgotPasswordState> = _state.asStateFlow()

    // 2. One-time Event Management (DÙNG CHUẨN AuthUiEvent)
    private val _uiEvent = Channel<AuthUiEvent>()
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

    // Send verificationcode
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
            _state.update { it.copy(isLoading = true) }

            // Gọi API kiểm tra Email & Gửi OTP
            val result = repository.verifyEmail(currentState.email)

            result.fold(
                onSuccess = {
                    _state.update { it.copy(isLoading = false, isCodeSent = true) }
                },
                onFailure = { error ->
                    _state.update { it.copy(isLoading = false, emailError = error.message) }
                }
            )
        }
    }

    // Submit verificationcode
    private fun verifyCode() {
        val currentState = _state.value

        if (currentState.verificationCode.isBlank()) {
            _state.update { it.copy(verificationError = "Please enter verification code") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            // Call Api to check
            val result = repository.verifyOtp(currentState.email, currentState.verificationCode)

            result.fold(
                onSuccess = {
                    _state.update { it.copy(isLoading = false) }
                    // Thành công OTP: Bắn event chở data sang màn CreateNewPassword
                    _uiEvent.trySend(AuthUiEvent.NavigateToCreateNewPassword(currentState.email, currentState.verificationCode))
                },
                onFailure = { error ->
                    _state.update { it.copy(isLoading = false, verificationError = error.message) }
                }
            )
        }
    }
}