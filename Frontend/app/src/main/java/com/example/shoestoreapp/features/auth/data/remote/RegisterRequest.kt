package com.example.shoestoreapp.features.auth.data.remote

data class RegisterRequest (
    val email: String,
    val password: String,
    val confirmPassword: String
)