// File: LoginResponse.kt
package com.example.shoestoreapp.features.auth.data.remote

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("token")
    val token: String,

    @SerializedName("message")
    val message: String,


)