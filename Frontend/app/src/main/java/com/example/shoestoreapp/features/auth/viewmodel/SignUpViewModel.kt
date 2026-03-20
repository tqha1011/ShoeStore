package com.example.shoestoreapp.features.auth.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import android.util.Patterns

class SignUpViewModel : ViewModel() {

    var email by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var confirmPassword by mutableStateOf("")
        private set

    var passwordVisible by mutableStateOf(false)
        private set

    var confirmPasswordVisible by mutableStateOf(false)
        private set

    var emailError by mutableStateOf<String?>(null)
        private set
    
    var passwordError by mutableStateOf<String?>(null)
        private set

    var confirmPasswordError by mutableStateOf<String?>(null)
        private set

    var isSignUpSuccessful by mutableStateOf(false)
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

    fun togglePasswordVisibility() {
        passwordVisible = !passwordVisible
    }

    fun toggleConfirmPasswordVisibility() {
        confirmPasswordVisible = !confirmPasswordVisible
    }

    private fun validateEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun validatePassword(password: String): Int {
        if (password.length < 8) {
            return 1
        }
        if (!password.any { it.isUpperCase() } ||
            !password.any { it.isLowerCase() } ||
            !password.any { it.isDigit() } ||
            !password.any { !it.isLetterOrDigit() }
        ) {
            return 2
        }
        return 0
    }


    fun onSignUpClick() {
        var hasError = false
        if (!validateEmail(email)) {
            emailError = "Invalid email format"
            hasError = true
        } else {
            emailError = null
        }

        when (validatePassword(password)) {
            0 -> {
                passwordError = null
            }

            1 -> {
                passwordError = "Password must be at least 8 characters long"
                hasError = true
            }

           2 -> {
               passwordError = "Password must contain at least 1 uppercase, 1 lowercase, 1 digit, and 1 special character (@, #, $, ...)"
               hasError = true
           }
        }
        
        if (password != confirmPassword) {
            confirmPasswordError = "Passwords do not match"
            hasError = true
        } else {
            confirmPasswordError = null
        }

        if (!hasError) {
            // Mock API check
            isSignUpSuccessful = true
        }
    }

    fun resetState() {
        isSignUpSuccessful = false
    }
}