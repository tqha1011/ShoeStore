package com.example.shoestoreapp.features.auth.data.remote

import com.google.gson.annotations.SerializedName

data class FacebookAuthDto(
    // C# Backend expects a property named "accessToken"
    @SerializedName("accessToken")
    val accessToken: String
)