package com.example.shoestoreapp.features.user.profile.data.remote

import com.google.gson.annotations.SerializedName

data class ChangePasswordDto(
    @SerializedName("oldPassword") val oldPassword: String,
    @SerializedName("newPassword") val newPassword: String,
    @SerializedName("confirmPassword") val confirmPassword: String
)

