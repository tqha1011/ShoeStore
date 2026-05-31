package com.example.shoestoreapp.features.user.voucher.data.remote

import com.google.gson.annotations.SerializedName

data class VoucherUserDto(
    @SerializedName("voucherGuid") val voucherGuid: String?,
    @SerializedName("voucherId") val voucherId: Int?,
    @SerializedName("voucherName") val voucherName: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("discount") val discount: Double?,
    @SerializedName("discountType") val discountType: String?,
    @SerializedName("voucherScope") val voucherScope: String?,
    @SerializedName("validTo") val validTo: String?,
    @SerializedName("savedAt") val savedAt: String?,
    @SerializedName("isUsed") val isUsed: Boolean?
)