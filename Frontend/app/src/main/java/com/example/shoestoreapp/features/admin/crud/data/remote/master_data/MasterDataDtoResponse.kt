package com.example.shoestoreapp.features.admin.crud.data.remote.master_data

import com.google.gson.annotations.SerializedName

// 1. DTO cho Size
data class SizeDto(
    @SerializedName("id") val id: String,
    @SerializedName("size") val sizeValue: String
)

// 2. DTO cho Color
data class ColorDto(
    @SerializedName("id") val id: String,
    @SerializedName("colorName") val colorName: String
)

// 3. DTO cho Category
data class CategoryDto(
    @SerializedName("id") val id: String,
    @SerializedName("categoryName") val categoryName: String
)