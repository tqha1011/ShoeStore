package com.example.shoestoreapp.features.user.voucher.data.models

data class VoucherUiModel(
    val id: String,
    val numericId: Int,
    val discountValue: String,
    val scope: String,
    val title: String,
    val description: String,
    val expiryDate: String,
    val isCollected: Boolean = false,
    val minOrderPrice: Double,
    val isUsed: Boolean = false
)