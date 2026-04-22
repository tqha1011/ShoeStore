package com.example.shoestoreapp.features.invoice.data.remote


import com.google.gson.annotations.SerializedName

data class InvoiceDetailsDto(
    @SerializedName("data")
    val `data`: List<Data?>?,
    @SerializedName("message")
    val message: String?
)