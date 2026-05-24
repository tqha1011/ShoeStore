package com.example.shoestoreapp.features.admin.voucher.data.remote

import com.google.gson.annotations.SerializedName

data class VoucherSearchResponseDto(
    @SerializedName("hasNext") val hasNext: Boolean,
    @SerializedName("hasPrevious") val hasPrevious: Boolean,
    @SerializedName("items") val items: List<ResponseVoucherAdminDto>,
    @SerializedName("pageNumber") val pageNumber: Int,
    @SerializedName("pageSize") val pageSize: Int,
    @SerializedName("totalCount") val totalCount: Int,
    @SerializedName("totalPages") val totalPages: Int
)

data class ResponseVoucherAdminDto(
    @SerializedName("voucherGuid") val voucherGuid: String,
    @SerializedName("voucherName") val voucherName: String,
    @SerializedName("voucherDescription") val voucherDescription: String? = null,
    @SerializedName("discount") val discount: Double,
    @SerializedName("discountType") val discountType: String?,
    @SerializedName("maxPriceDiscount") val maxPriceDiscount: Double,
    @SerializedName("minOrderPrice") val minOrderPrice: Double,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("validFrom") val validFrom: String,
    @SerializedName("validTo") val validTo: String,
    @SerializedName("voucherScope") val voucherScope: String?
)
