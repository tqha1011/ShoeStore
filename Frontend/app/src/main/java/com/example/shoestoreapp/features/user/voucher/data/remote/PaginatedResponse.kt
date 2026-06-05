package com.example.shoestoreapp.features.user.voucher.data.remote

import com.google.gson.annotations.SerializedName

data class PaginatedResponse<T>(
    @SerializedName("hasNext") val hasNext: Boolean,
    @SerializedName("hasPrevious") val hasPrevious: Boolean,
    @SerializedName("items") val items: List<T>,
    @SerializedName("pageNumber") val pageNumber: Int,
    @SerializedName("pageSize") val pageSize: Int,
    @SerializedName("totalCount") val totalCount: Int,
    @SerializedName("totalPages") val totalPages: Int
)

