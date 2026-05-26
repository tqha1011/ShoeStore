package com.example.shoestoreapp.features.user.voucher.data.remote

data class PagedUserVoucherResponse(
    val items: List<ResponseVoucherUserDto>?,
    val hasNext: Boolean,
    val hasPrevious: Boolean,
    val pageNumber: Int,
    val pageSize: Int,
    val totalCount: Int,
    val totalPages: Int
)

data class ResponseVoucherUserDto(
    val voucherId: Int,
    val voucherGuid: String,
    val voucherName: String?,
    val description: String?,
    val discount: Double,
    val discountType: Int,
    val voucherScope: Int,
    val minOrderPrice: Double,
    val isUsed: Boolean,
    val savedAt: String?,
    val validFrom: String?,
    val validTo: String?
)

