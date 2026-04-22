package com.example.shoestoreapp.features.invoice.data.remote


import com.google.gson.annotations.SerializedName

data class UpdateStatusRequestDto(
    @SerializedName("status")
    val status: Int?
)