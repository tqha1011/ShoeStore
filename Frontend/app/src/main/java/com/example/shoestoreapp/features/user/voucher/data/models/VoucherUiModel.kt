package com.example.shoestoreapp.features.user.voucher.data.models

enum class VoucherDiscountType {
    PERCENTAGE,
    FIXED,
    FREESHIP
}

enum class VoucherStatus {
    AVAILABLE,
    USED,
    EXPIRED
}

data class VoucherUiModel(
    val id: String,
    val title: String,
    val description: String,
    val expiryDate: String,
    val discountValue: String,
    val discountType: VoucherDiscountType,
    val scopeSubtitle: String,
    val status: VoucherStatus
)


