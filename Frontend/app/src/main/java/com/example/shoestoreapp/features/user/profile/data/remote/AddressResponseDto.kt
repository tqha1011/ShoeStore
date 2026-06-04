package com.example.shoestoreapp.features.user.profile.data.remote

import com.google.gson.annotations.SerializedName

data class AddressResponseDto(
    @SerializedName("id") val id: String,
    @SerializedName("address") val address: String,
    @SerializedName("isDefault") val isDefault: Boolean
)

