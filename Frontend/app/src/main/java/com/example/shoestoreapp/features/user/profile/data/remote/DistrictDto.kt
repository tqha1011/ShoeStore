package com.example.shoestoreapp.features.user.profile.data.remote

data class DistrictDto(
    val code: Int,
    val name: String,
    val wards: List<WardDto>? = null
)

