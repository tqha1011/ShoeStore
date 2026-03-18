package com.example.shoestoreapp.features.auth.viewmodel

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class ForgotPasswordModel : ViewModel() {
    var email by mutableStateOf("")
        private set
    
    var verificationCode by mutableStateOf("")
        private set

    var isCodeSent by mutableStateOf(false)
        private set

    var emailError by mutableStateOf<String?>(null)
        private set

    fun onEmailChange(newValue: String) {
        email = newValue
        emailError = null
    }
    private fun validateEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    fun onVerificationCodeChange(newValue: String) {
        verificationCode = newValue
    }

    fun sendRequest() {

        if (email.isNotBlank() && validateEmail(email)) {
            // Simulate sending request
            isCodeSent = true
        } else {
            emailError = "Please enter your email"
        }
    }

    fun resetState() {
        email = ""
        verificationCode = ""
        isCodeSent = false
        emailError = null
    }
}