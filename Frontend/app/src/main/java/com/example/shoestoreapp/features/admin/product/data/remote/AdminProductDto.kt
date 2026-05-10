package com.example.shoestoreapp.features.admin.product.data.remote

import com.google.gson.annotations.SerializedName

data class ProductResponseDto(
    @SerializedName("publicId") val publicId: String,
    @SerializedName("productName") val productName: String,
    @SerializedName("brand") val brand: String,
    @SerializedName("categoryId") val categoryId: Int,
    @SerializedName("categoryName") val categoryName: String,
    @SerializedName("variants") val variants: List<ProductVariantResponseDto>
)

data class ProductVariantResponseDto(
    @SerializedName("publicId") val publicId: String,
    @SerializedName("colorId") val colorId: Int,
    @SerializedName("colorName") val colorName: String?,
    @SerializedName("imageUrl") val imageUrl: String?,
    @SerializedName("isDelete") val isDelete: Boolean,
    @SerializedName("isSelling") val isSelling: Boolean,
    @SerializedName("price") val price: Double,
    @SerializedName("size") val size: Double,
    @SerializedName("sizeId") val sizeId: Int,
    @SerializedName("stock") val stock: Int
)

