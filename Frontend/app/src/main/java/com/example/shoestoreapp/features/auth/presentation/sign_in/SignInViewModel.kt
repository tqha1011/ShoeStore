package com.example.shoestoreapp.features.auth.presentation.sign_in

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestoreapp.core.networks.RetrofitInstance
import com.example.shoestoreapp.core.utils.JwtUtils
import com.example.shoestoreapp.core.utils.TokenManager
import com.example.shoestoreapp.features.auth.data.remote.LoginRequest // Chú ý import từ DTO nhé
import com.example.shoestoreapp.features.auth.data.repository.AuthRepositoryImpl
import com.example.shoestoreapp.features.auth.domain.repository.AuthRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SignInViewModel(
    private val repository: AuthRepository = AuthRepositoryImpl(RetrofitInstance.authApi),
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(SignInState())
    val state: StateFlow<SignInState> = _state.asStateFlow()

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

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

        if (currentState.email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(currentState.email).matches()) {
            emailErr = "Invalid email format"
            hasError = true
        }
        if (currentState.password.isEmpty()) {
            passErr = "Password cannot be empty"
            hasError = true
        }
        if (hasError) {
            _state.update { it.copy(
                emailError = emailErr,
                passwordError = passErr,
                email = if (emailErr != null) "" else it.email,
                password = if (passErr != null) "" else it.password
            )}
            return
        }

        // Validate OK -> Gọi API thật
        callSignInApi()
    }

    private fun callSignInApi() {
        viewModelScope.launch {
            val currentState = _state.value

            // 1. Bật loading spinner lên
            _state.update { it.copy(isLoading = true) }

            // 2. Đóng gói dữ liệu thành DTO để gửi đi
            val request = LoginRequest(
                email = currentState.email,
                password = currentState.password
            )

            // 3. Quản lý kho (Repository) đem xe tải (Retrofit) chở hàng lên Server
            val result = repository.login(request)

            // 4. Có kết quả trả về -> Tắt loading
            _state.update { it.copy(isLoading = false) }

            // 5. Mở hộp quà Result xem Thành công hay Thất bại
            result.fold(
                onSuccess = { response ->
                    val token = response.token

                    // 1. Mổ Token lấy Role
                    val role = JwtUtils.getRoleFromToken(token)

                    // 2. Cất cả Token và Role vào Két sắt DataStore
                    tokenManager.saveAuthInfo(token = token, role = role)

                    // 3. Bẻ lái (Chuyển trang) tùy theo quyền
                    if (role.uppercase() == "ADMIN") {
                        _uiEvent.send(UiEvent.NavigateToAdminDashboard)
                    } else {
                        _uiEvent.send(UiEvent.NavigateToSignUp)
                    }
                },
                onFailure = { error ->
                    _state.update { it.copy(
                        isLoading = false,
                        emailError = null, // Xóa lỗi email đi
                        passwordError = error.message // Nhét lỗi xuống khung Password
                    )}
                }
            )
        }
    }

    // ĐÃ FIX: Thêm NavigateToUserHome và NavigateToAdminDashboard vào đây
    sealed interface UiEvent {
        object NavigateToUserHome : UiEvent
        object NavigateToAdminDashboard : UiEvent
        object NavigateToSignUp : UiEvent
        data class ShowError(val message: String) : UiEvent
    }
}