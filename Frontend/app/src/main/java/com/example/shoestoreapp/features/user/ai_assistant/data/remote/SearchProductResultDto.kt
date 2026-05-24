package com.example.shoestoreapp.features.user.ai_assistant.data.remote

import com.example.shoestoreapp.features.user.product.data.remote.ProductResponseDto
import com.google.gson.annotations.SerializedName

data class SearchProductResultDto(
    @SerializedName("status", alternate = ["Status"])
    val status : String? = null,

    @SerializedName("message", alternate = ["Message"])
    val message : String? = null,

    @SerializedName("products", alternate = ["Products"])
    val products : List<ProductResponseDto>? = null
)
