package com.example.shoestoreapp.features.admin.voucher.viewmodel

data class VoucherUiState(
    val voucherName: String = "",
    val description: String = "",
    val targetApplication: Int = 0,
    val discountStyle: Int = 0,
    val discountValue: String = "",
    val maxReduction: String = "",
    val minOrder: String = "",
    val totalQuantity: String = "",
    val maxUsagePerUser: String = "",
    val validFrom: String = "",
    val validTo: String = "",
    val isLoading: Boolean = false
)
