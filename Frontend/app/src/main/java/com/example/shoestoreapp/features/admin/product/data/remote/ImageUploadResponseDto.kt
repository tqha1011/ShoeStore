package com.example.shoestoreapp.features.admin.product.data.remote

import com.google.gson.annotations.SerializedName

data class ImageUploadResponseDto(
    @SerializedName("message") val message: String?,
    @SerializedName("imageUrl") val imageUrl: String
)

