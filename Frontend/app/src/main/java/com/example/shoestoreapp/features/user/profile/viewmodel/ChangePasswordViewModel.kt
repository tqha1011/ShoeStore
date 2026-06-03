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
import retrofit2.HttpException
import org.json.JSONObject

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
                    val realErrorMessage = if (throwable is HttpException) {
                        parseValidationError(throwable)
                    } else {
                        throwable.message ?: "Change password failed."
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            bannerMessage = realErrorMessage,
                            isBannerSuccess = false,
                            showBanner = true
                        )
                    }
                }
        }
    }

    // Hàm phụ trợ lấy lỗi chi tiết
    private fun parseValidationError(exception: HttpException): String {
        return try {
            val errorString = exception.response()?.errorBody()?.string()
            if (!errorString.isNullOrEmpty()) {
                val jsonObject = JSONObject(errorString)

                // Ưu tiên 1: Lấy thông tin từ object errors
                if (jsonObject.has("errors")) {
                    val errorsObj = jsonObject.getJSONObject("errors")
                    val keys = errorsObj.keys()
                    if (keys.hasNext()) {
                        val firstKey = keys.next()
                        val errorArray = errorsObj.getJSONArray(firstKey)
                        if (errorArray.length() > 0) {
                            return errorArray.getString(0)
                        }
                    }
                }

                // Ưu tiên 2: Nếu không có mảng errors thì lấy title
                if (jsonObject.has("title")) {
                    return jsonObject.getString("title")
                }

                // Ưu tiên 3: Lấy message thông thường
                if (jsonObject.has("message")) {
                    return jsonObject.getString("message")
                }
            }
            "Change password failed (HTTP ${exception.code()})"
        } catch (e: Exception) {
            "Change password failed (HTTP ${exception.code()})"
        }
    }
}