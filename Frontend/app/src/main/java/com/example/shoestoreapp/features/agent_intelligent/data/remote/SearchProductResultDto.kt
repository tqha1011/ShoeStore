package com.example.shoestoreapp.features.agent_intelligent.data.remote

import com.example.shoestoreapp.features.user.product.data.remote.ProductResponseDto
import com.google.gson.annotations.SerializedName

data class SearchProductResultDto(
    @SerializedName("status", alternate = ["Status"])
    val status : String? = null,

    @SerializedName("message", alternate = ["Message"])
    val message : String? = null,

    @SerializedName(
        "products",
        alternate = ["Products", "items", "Items", "data", "Data", "results", "Results"]
    )
    val products : List<ProductSummaryForLlm>? = null
)

data class ProductSummaryForLlm(
    @SerializedName("publicId", alternate = ["PublicId", "productGuid", "ProductGuid"])
    val publicId : String? = null,

    @SerializedName("productName", alternate = ["ProductName", "name", "Name"])
    val productName : String? = null,

    @SerializedName("productBrand", alternate = ["ProductBrand", "brand", "Brand"])
    val productBrand : String? = null,

    @SerializedName("catagoryName", alternate = ["CatagoryName", "CategoryName", "categoryName"])
    val catagoryName : String? = null
)
