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
    val errorMessage: String? = null
)

class ChangePasswordViewModel(
    private val repository: ProfileRepository = ProfileRepositoryImpl()
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState: StateFlow<ChangePasswordUiState> = _uiState.asStateFlow()

    private val passwordPattern = "^(?=.*[A-Z])(?=.*\\d).{8,}$".toRegex()
    val hasMinLength = MutableStateFlow(false)
    val hasUpperCase = MutableStateFlow(false)
    val hasDigit = MutableStateFlow(false)
    val isPasswordValid = MutableStateFlow(false)

    fun onOldPasswordChanged(value: String) {
        _uiState.update { it.copy(oldPassword = value) }
    }

    fun onNewPasswordChanged(value: String) {
        _uiState.update { it.copy(newPassword = value) }
        hasMinLength.value = value.length >= 8
        hasUpperCase.value = value.any { it.isUpperCase() }
        hasDigit.value = value.any { it.isDigit() }
        isPasswordValid.value = passwordPattern.matches(value)
    }

    fun onConfirmPasswordChanged(value: String) {
        _uiState.update { it.copy(confirmPassword = value) }
    }

    fun onChangePasswordClicked() {
        val current = _uiState.value
        if (current.oldPassword.isBlank() || current.newPassword.isBlank() || current.confirmPassword.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please fill all fields") }
            return
        }
        if (!isPasswordValid.value) {
            _uiState.update { it.copy(errorMessage = "Password must be at least 8 characters and include 1 uppercase letter and 1 digit") }
            return
        }
        if (current.newPassword != current.confirmPassword) {
            _uiState.update { it.copy(errorMessage = "New passwords do not match") }
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
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Change password failed."
                        )
                    }
                }
        }
    }
}
