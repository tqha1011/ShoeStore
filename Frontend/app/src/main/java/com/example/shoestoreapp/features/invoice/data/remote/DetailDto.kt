package com.example.shoestoreapp.features.invoice.data.remote


import com.google.gson.annotations.SerializedName

data class DetailDto(
    @SerializedName("color")
    val color: String?,
    @SerializedName("imageUrl")
    val imageUrl: String?,
    @SerializedName("productName")
    val productName: String?,
    @SerializedName("quantity")
    val quantity: Int?,
    @SerializedName("size")
    val size: Int?,
    @SerializedName("unitPrice")
    val unitPrice: Int?
)