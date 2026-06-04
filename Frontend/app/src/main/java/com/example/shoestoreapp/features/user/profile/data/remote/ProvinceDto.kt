package com.example.shoestoreapp.features.user.profile.data.remote

data class ProvinceDto(
    val code: Int,
    val name: String,
    val districts: List<DistrictDto>? = null
)

