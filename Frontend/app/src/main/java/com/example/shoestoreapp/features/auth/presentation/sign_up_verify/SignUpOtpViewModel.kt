package com.example.shoestoreapp.features.auth.presentation.sign_up_verify

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestoreapp.core.networks.RetrofitInstance
import com.example.shoestoreapp.features.auth.data.repository.AuthRepositoryImpl
import com.example.shoestoreapp.features.auth.domain.repository.AuthRepository
import com.example.shoestoreapp.features.auth.presentation.components.AuthUiEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SignUpOtpViewModel(
    private val email: String,
    private val repository: AuthRepository = AuthRepositoryImpl(RetrofitInstance.authApi)
) : ViewModel() {

    private val _state = MutableStateFlow(SignUpOtpState())
    val state: StateFlow<SignUpOtpState> = _state.asStateFlow()

    private val _uiEvent = Channel<AuthUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    fun onEvent(event: SignUpOtpEvent) {
        when (event) {
            is SignUpOtpEvent.OtpChanged -> {
                _state.update { it.copy(otpCode = event.otp, otpError = null) }
            }
            SignUpOtpEvent.Submit -> verifyOtp()
        }
    }

    private fun verifyOtp() {
        val currentState = _state.value
        val otp = currentState.otpCode.trim()

        if (otp.length != 6) {
            _state.update { it.copy(otpError = "Please enter the 6-digit verification code") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, otpError = null) }

            val result = repository.verifySignUpOtp(email, otp)

            result.fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Email verified successfully. Please sign in."
                        )
                    }
                    _uiEvent.trySend(AuthUiEvent.NavigateToSignIn)
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            otpError = error.message ?: "Verification failed. Please try again."
                        )
                    }
                }
            )
        }
    }
}
