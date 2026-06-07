package com.example.shoestoreapp.features.user.profile.data.remote

import com.google.gson.annotations.SerializedName

data class UpdateProfileDto(
    @SerializedName("userName") val userName: String,
    @SerializedName("avatarUrl") val avatarUrl: String?,
    @SerializedName("dateOfBirth") val dateOfBirth: String?
)

