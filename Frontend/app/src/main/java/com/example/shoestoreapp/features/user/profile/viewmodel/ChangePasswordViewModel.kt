package com.example.shoestoreapp.features.user.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestoreapp.features.user.profile.data.remote.ChangePasswordDto
import com.example.shoestoreapp.features.user.profile.data.repositories.ProfileRepository
import com.example.shoestoreapp.features.user.profile.data.repositories.ProfileRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChangePasswordUiState(
    val oldPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val bannerMessage: String = "",
    val isBannerSuccess: Boolean = true,
    val showBanner: Boolean = false
)

class ChangePasswordViewModel(
    private val repository: ProfileRepository = ProfileRepositoryImpl()
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState: StateFlow<ChangePasswordUiState> = _uiState.asStateFlow()

    private val passwordPattern = "^(?=.*[A-Z])(?=.*\\d).{8,}$".toRegex()

    private val _hasMinLength = MutableStateFlow(false)
    val hasMinLength: StateFlow<Boolean> = _hasMinLength.asStateFlow()

    private val _hasUpperCase = MutableStateFlow(false)
    val hasUpperCase: StateFlow<Boolean> = _hasUpperCase.asStateFlow()

    private val _hasDigit = MutableStateFlow(false)
    val hasDigit: StateFlow<Boolean> = _hasDigit.asStateFlow()

    private val _isPasswordValid = MutableStateFlow(false)
    val isPasswordValid: StateFlow<Boolean> = _isPasswordValid.asStateFlow()

    fun onOldPasswordChanged(value: String) {
        _uiState.update { it.copy(oldPassword = value) }
    }

    fun onNewPasswordChanged(value: String) {
        _uiState.update { it.copy(newPassword = value) }
        _hasMinLength.value = value.length >= 8
        _hasUpperCase.value = value.any { it.isUpperCase() }
        _hasDigit.value = value.any { it.isDigit() }
        _isPasswordValid.value = passwordPattern.matches(value)
    }

    fun onConfirmPasswordChanged(value: String) {
        _uiState.update { it.copy(confirmPassword = value) }
    }

    fun hideBanner() {
        _uiState.update { it.copy(showBanner = false) }
    }

    fun onChangePasswordClicked() {
        val current = _uiState.value
        if (current.oldPassword.isBlank() || current.newPassword.isBlank() || current.confirmPassword.isBlank()) {
            _uiState.update {
                it.copy(
                    bannerMessage = "Please fill all fields",
                    isBannerSuccess = false,
                    showBanner = true
                )
            }
            return
        }
        if (!_isPasswordValid.value) {
            _uiState.update {
                it.copy(
                    bannerMessage = "Password must be at least 8 characters and include 1 uppercase letter and 1 digit",
                    isBannerSuccess = false,
                    showBanner = true
                )
            }
            return
        }
        if (current.newPassword != current.confirmPassword) {
            _uiState.update {
                it.copy(
                    bannerMessage = "New passwords do not match",
                    isBannerSuccess = false,
                    showBanner = true
                )
            }
            return
        }
        if (current.isLoading) return

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val dto = ChangePasswordDto(
                oldPassword = current.oldPassword,
                newPassword = current.newPassword,
                confirmPassword = current.confirmPassword
            )
            repository.changePassword(dto)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = true,
                            bannerMessage = "Password changed successfully",
                            isBannerSuccess = true,
                            showBanner = true
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            bannerMessage = throwable.message ?: "Change password failed.",
                            isBannerSuccess = false,
                            showBanner = true
                        )
                    }
                }
        }
    }
}