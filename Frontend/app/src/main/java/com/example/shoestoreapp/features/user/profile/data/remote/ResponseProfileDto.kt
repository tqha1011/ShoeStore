package com.example.shoestoreapp.features.user.profile.data.remote

import com.google.gson.annotations.SerializedName

data class ResponseProfileDto(
    @SerializedName("userGuid") val userGuid: String,
    @SerializedName("userName") val userName: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("avatarUrl") val avatarUrl: String?,
    @SerializedName("dateOfBirth") val dateOfBirth: String?
)

