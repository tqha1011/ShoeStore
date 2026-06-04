package com.example.shoestoreapp.features.admin.voucher.data.model

data class VoucherCampaign(
    val id: String,
    val name: String,
    val targetApplication: Int,
    val discountStyle: Int,
    val discountValue: String,
    val maxReduction: String,
    val minOrder: String,
    val validTo: String
)