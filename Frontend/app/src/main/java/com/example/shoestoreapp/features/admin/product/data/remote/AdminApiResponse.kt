package com.example.shoestoreapp.features.admin.product.data.remote

import com.google.gson.annotations.SerializedName


// 1. DTO cho Response tổng quát
data class ProductSearchResponse(
    @SerializedName("totalCount") val totalCount: Int,
    @SerializedName("pageNumber") val pageNumber: Int,
    @SerializedName("pageSize") val pageSize: Int,
    @SerializedName("totalPages") val totalPages: Int,
    @SerializedName("hasPrevious") val hasPrevious: Boolean,
    @SerializedName("hasNext") val hasNext: Boolean,
    @SerializedName("items") val items: List<ProductSearchDto>
)

// 2. DTO cho từng sản phẩm
data class ProductSearchDto(
    @SerializedName("publicId") val publicId: String?,
    @SerializedName("productName") val productName: String?,
    @SerializedName("variants") val variants: List<ProductVariantDto>?
)

// 4. DTO cho biến thể sản phẩm
data class ProductVariantDto(
    @SerializedName("stockStatus") val stockStatus: String?,
    @SerializedName("stock") val stock: Int?,
    @SerializedName("price") val price: Double?,
    @SerializedName("imageUrl") val imageUrl: String?,
)