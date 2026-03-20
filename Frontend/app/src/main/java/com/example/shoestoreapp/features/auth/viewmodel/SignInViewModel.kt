package com.example.shoestoreapp.features.auth.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import android.util.Patterns

class SignInViewModel : ViewModel(){
    var email by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var emailError by mutableStateOf<String?>(null)
        private set

    var passwordError by mutableStateOf<String?>(null)
        private set

    var passwordVisible by mutableStateOf(false)
        private set

    // State to notify UI of a successful sign in
    var isSignInSuccessful by mutableStateOf(false)
        private set

    fun onEmailChange(newValue: String) {
        email = newValue
        emailError = null
    }

    fun onPasswordChange(newValue: String) {
        password = newValue
        passwordError = null
    }

    fun onTogglePasswordVisibility() {
        passwordVisible = !passwordVisible
    }
    private fun validateEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun onSignInClick() {
        var hasError = false

        // Validate Email
        if (!validateEmail(email)) {
            emailError = "Invalid email format"
            hasError = true
            email = ""
            password = ""
        } else {
            emailError = null
        }

        // Validate Password
        if (password.isEmpty()) {
            passwordError = "Password cannot be empty"
            hasError = true
        } else {
            passwordError = null
        }

        if (!hasError) {
            // Mock logic for API call...
            // After success:
            isSignInSuccessful = true
        }
    }

    fun resetState() {
        isSignInSuccessful = false
        email = ""
        password = ""
        emailError = null
        passwordError = null
    }
}
