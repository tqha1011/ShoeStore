package com.example.shoestoreapp.features.invoice.data.remote


import com.google.gson.annotations.SerializedName

data class InvoiceListDto(
    @SerializedName("hasNext")
    val hasNext: Boolean?,
    @SerializedName("hasPrevious")
    val hasPrevious: Boolean?,
    @SerializedName("items")
    val itemDto: List<ItemDto>?,
    @SerializedName("pageNumber")
    val pageNumber: String?,
    @SerializedName("pageSize")
    val pageSize: String?,
    @SerializedName("totalCount")
    val totalCount: String?,
    @SerializedName("totalPages")
    val totalPages: String?
)