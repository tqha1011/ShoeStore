package com.example.shoestoreapp.features.invoice.data.remote


import com.google.gson.annotations.SerializedName

data class InvoiceDetailsDto(
    @SerializedName("data")
    val detailDto: List<DetailDto>?,
    @SerializedName("message")
    val message: String?
)