// File: LoginResponse.kt
package com.example.shoestoreapp.features.auth.data.remote

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    // Cái chữ trong ngoặc "access_token" là tên biến mà Server trả về
    @SerializedName("access_token")
    val token: String,

    @SerializedName("message")
    val message: String,


)