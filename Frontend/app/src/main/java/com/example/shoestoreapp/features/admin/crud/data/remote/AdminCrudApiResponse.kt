package com.example.shoestoreapp.features.admin.crud.data.remote

import com.google.gson.annotations.SerializedName

// DTO cho biến thể sản phẩm
data class ProductVariantDto(
    @SerializedName("stockStatus") val stockStatus: String?,
    @SerializedName("stock") val stock: Int?,
    @SerializedName("price") val price: Double?,
    @SerializedName("imageUrl") val imageUrl: String?,
)