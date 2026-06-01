package com.example.shoestoreapp.features.user.profile.data.remote

data class CreateAddressDto(
    val detailAddress: String,
    val districtId: Int,
    val isDefault: Boolean,
    val provinceId: Int,
    val wardId: Int
)

