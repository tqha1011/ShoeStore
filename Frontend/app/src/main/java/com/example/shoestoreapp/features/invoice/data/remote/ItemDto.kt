package com.example.shoestoreapp.features.invoice.data.remote


import com.google.gson.annotations.SerializedName

data class ItemDto(
    @SerializedName("address")
    val address: String?,
    @SerializedName("dateCreated")
    val dateCreated: String?,
    @SerializedName("finalPrice")
    val finalPrice: String?,
    @SerializedName("orderCode")
    val orderCode: String?,
    @SerializedName("paymentName")
    val paymentName: String?,
    @SerializedName("phone")
    val phone: String?,
    @SerializedName("publicId")
    val publicId: String?,
    @SerializedName("status")
    val status: Int?,
    @SerializedName("updateCreated")
    val updateCreated: Any?,
    @SerializedName("username")
    val username: String?
)