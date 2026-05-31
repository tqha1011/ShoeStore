package com.example.shoestoreapp.features.admin.voucher.data.remote

import com.google.gson.annotations.SerializedName

data class CreateVoucherDto(
    @SerializedName("voucherName") val voucherName: String,
    @SerializedName("voucherDescription") val voucherDescription: String,
    @SerializedName("voucherScope") val voucherScope: Int,
    @SerializedName("discountType") val discountType: Int,
    @SerializedName("discount") val discount: Double,
    @SerializedName("maxPriceDiscount") val maxPriceDiscount: Double,
    @SerializedName("minOrderPrice") val minOrderPrice: Double,
    @SerializedName("totalQuantity") val totalQuantity: Int,
    @SerializedName("maxUsagePerUser") val maxUsagePerUser: Int,
    @SerializedName("releaseType") val releaseType: Int,
    @SerializedName("validFrom") val validFrom: String,
    @SerializedName("validTo") val validTo: String
)
