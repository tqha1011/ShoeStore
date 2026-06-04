package com.example.shoestoreapp.features.user.voucher.data.remote

import com.google.gson.annotations.SerializedName

data class VoucherDto(
    @SerializedName("voucherGuid") val voucherGuid: String?,
    @SerializedName("voucherName") val voucherName: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("discount") val discount: Double?,
    @SerializedName("discountType") val discountType: String?,
    @SerializedName("maxPriceDiscount") val maxPriceDiscount: Double?,
    @SerializedName("minOrderPrice") val minOrderPrice: Double?,
    @SerializedName("quantity") val quantity: Int?,
    @SerializedName("validFrom") val validFrom: String?,
    @SerializedName("validTo") val validTo: String?,
    @SerializedName("voucherScope") val voucherScope: String?
)