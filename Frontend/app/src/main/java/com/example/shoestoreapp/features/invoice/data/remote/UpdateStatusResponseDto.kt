package com.example.shoestoreapp.features.invoice.data.remote

import com.google.gson.annotations.SerializedName

data class UpdateStatusResponseDto (
    @SerializedName("invoiceCode")
    val status: String,
    @SerializedName ("invoiceStatus")
    val invoiceStatus: Int
)