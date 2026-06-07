package com.example.shoestoreapp.features.agent_intelligent.data.remote

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class AddVariantResultDto(
    @SerializedName("status", alternate = ["Status"])
    val status : String? = null,

    @SerializedName("message", alternate = ["Message"])
    val message : String? = null,

    @SerializedName("variant", alternate = ["Variant"])
    val variant : VariantResultDto?
)

data class VariantResultDto(
    @SerializedName("productId", alternate = ["ProductId"])
    val productId : String? = null,

    @SerializedName("sizeId", alternate = ["SizeId"])
    val sizeId : Int? = null,

    @SerializedName("size", alternate = ["Size"])
    val size : BigDecimal? = null,

    @SerializedName("colorId", alternate = ["ColorId"])
    val colorId : Int? = null,

    @SerializedName("colorName", alternate = ["ColorName"])
    val colorName : String? = null,

    @SerializedName("stock", alternate = ["Stock"])
    val stock : Int? = null,

    @SerializedName("price", alternate = ["Price"])
    val price : BigDecimal? = null,

    @SerializedName("imageUrl", alternate = ["ImageUrl"])
    val imageUrl : String? = null
)
