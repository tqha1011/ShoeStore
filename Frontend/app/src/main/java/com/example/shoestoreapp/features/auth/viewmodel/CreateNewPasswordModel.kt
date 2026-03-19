package com.example.shoestoreapp.features.auth.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class CreateNewPasswordModel : ViewModel() {
    var password by mutableStateOf("")
        private set
    
    var confirmPassword by mutableStateOf("")
        private set

    var passwordVisible by mutableStateOf(false)
        private set

    var confirmPasswordVisible by mutableStateOf(false)
        private set

    var passwordError by mutableStateOf<String?>(null)
        private set

    // State to notify UI of a successful reset
    var isResetSuccessful by mutableStateOf(false)
        private set

    fun onPasswordChange(newValue: String) {
        password = newValue
        passwordError = null
    }

    fun onConfirmPasswordChange(newValue: String) {
        confirmPassword = newValue
        passwordError = null
    }

    fun togglePasswordVisibility() {
        passwordVisible = !passwordVisible
    }

    fun toggleConfirmPasswordVisibility() {
        confirmPasswordVisible = !confirmPasswordVisible
    }

    fun validateAndSubmit() {
        if (password.length < 6) {
            passwordError = "Password must be at least 6 characters long"
            return
        }
        if (password != confirmPassword) {
            passwordError = "Passwords do not match"
            return
        }
        
        // Mock logic for API call...
        // After success:
        isResetSuccessful = true
    }

    fun resetState() {
        isResetSuccessful = false
    }
}