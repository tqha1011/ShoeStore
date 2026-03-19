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

    var isVerificationSuccessful by mutableStateOf(false)
        private set

    var emailError by mutableStateOf<String?>(null)
        private set

    var verificationError by mutableStateOf<String?>(null)
        private set

    fun onEmailChange(newValue: String) {
        email = newValue
        emailError = null
    }

    fun onVerificationCodeChange(newValue: String) {
        verificationCode = newValue
        verificationError = null
    }

    private fun validateEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun sendRequest() {
        if (email.isBlank()) {
            emailError = "Please enter your email"
            return
        }
        
        if (!validateEmail(email)) {
            emailError = "Invalid email format"
            return
        }

        // Mock API call to send verification code
        isCodeSent = true
    }

    fun verifyCode() {

        // Mock API call to verify code
        isVerificationSuccessful = true
    }

    fun resetState() {
        email = ""
        verificationCode = ""
        isCodeSent = false
        isVerificationSuccessful = false
        emailError = null
        verificationError = null
    }
}