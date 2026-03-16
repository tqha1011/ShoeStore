package com.example.shoestoreapp.features.auth.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import android.util.Patterns

class RegisterViewModel : ViewModel() {

    var email by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var confirmPassword by mutableStateOf("")
        private set

    var emailError by mutableStateOf<String?>(null)
        private set

    var passwordError by mutableStateOf<String?>(null)
        private set

    var confirmPasswordError by mutableStateOf<String?>(null)
        private set

    fun onEmailChange(newValue: String) {
        email = newValue
        emailError = null
    }

    fun onPasswordChange(newValue: String) {
        password = newValue
        passwordError = null
    }

    fun onConfirmPasswordChange(newValue: String) {
        confirmPassword = newValue
        confirmPasswordError = null
    }

    private fun validateEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun validatePassword(password: String): Boolean {
        return password.length >= 8
    }

    fun onSignUpClick(onSuccess: () -> Unit) {
        var isValid = true

        if (!validateEmail(email)) {
            emailError = "Định dạng email không hợp lệ"
            isValid = false
        } else {
            emailError = null
        }

        if (!validatePassword(password)) {
            passwordError = "Mật khẩu phải có ít nhất 8 ký tự"
            isValid = false
        } else {
            passwordError = null
        }

        if (password != confirmPassword) {
            confirmPasswordError = "Mật khẩu xác nhận không khớp"
            isValid = false
        } else {
            confirmPasswordError = null
        }

        if (isValid) {
            onSuccess() // Sign up successfully
        }
    }
}
