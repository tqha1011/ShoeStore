package com.example.shoestoreapp.features.user.profile.data.remote

data class CreateAddressDto(
    val detailAddress: String,
    val district: String,
    val isDefault: Boolean,
    val province: String,
    val ward: String
)

