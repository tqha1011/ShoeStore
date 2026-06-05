package com.example.shoestoreapp.features.admin.crud.data.remote

import com.google.gson.annotations.SerializedName

// DTO cho biến thể sản phẩm
data class ProductUpdateDtoRequest(
    @SerializedName("productName") val productName: String?,
    @SerializedName("brand") val brand: String?,
    @SerializedName("categoryId") val categoryId: String,
    @SerializedName("variants") val variants: List<ProductUpdateVariantDto>
)

data class ProductUpdateVariantDto(
    @SerializedName("publicId") val publicId: String?,
    @SerializedName("sizeId") val sizeId: Int,
    @SerializedName("colorId") val colorId: Int,
    @SerializedName("stock") val stock: Int,
    @SerializedName("price") val price: Double,
    @SerializedName("imageUrl") val imageUrl: String?,
    @SerializedName("isSelling") val isSelling: Boolean
)

data class ProductCreateDtoRequest(
    @SerializedName("productName") val productName: String,
    @SerializedName("brand") val brand: String,
    @SerializedName("categoryId") val categoryId: String,
    @SerializedName("variants") val variants: List<ProductCreateVariantDto>
)

data class ProductCreateVariantDto(
    @SerializedName("sizeId") val sizeId: Int,
    @SerializedName("size") val size: Int,
    @SerializedName("colorId") val colorId: Int,
    @SerializedName("colorName") val colorName: String,
    @SerializedName("stock") val stock: Int,
    @SerializedName("price") val price: Double,
    @SerializedName("imageUrl") val imageUrl: String,
    @SerializedName("isSelling") val isSelling: Boolean
)
