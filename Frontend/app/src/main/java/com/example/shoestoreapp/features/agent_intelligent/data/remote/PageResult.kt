package com.example.shoestoreapp.features.agent_intelligent.data.remote

import com.google.gson.annotations.SerializedName

data class PageResult<T>(
    @SerializedName("items")
    val items: List<T>,
    @SerializedName("totalCount")
    val totalCount: Int,
    @SerializedName("pageNumber")
    val pageNumber: Int,
    @SerializedName("pageSize")
    val pageSize: Int,
    @SerializedName("totalPages")
    val totalPages: Int,
    @SerializedName("hasPrevious")
    val hasPrevious: Boolean,
    @SerializedName("hasNext")
    val hasNext: Boolean
) {
}