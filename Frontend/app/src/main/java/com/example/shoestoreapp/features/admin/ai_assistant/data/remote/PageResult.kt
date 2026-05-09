package com.example.shoestoreapp.features.admin.ai_assistant.data.remote

import com.google.gson.annotations.SerializedName

data class PageResult<T>(
    @SerializedName("items")
    val items: List<T>,
    @SerializedName("totalCount")
    val totalCount: Int
)