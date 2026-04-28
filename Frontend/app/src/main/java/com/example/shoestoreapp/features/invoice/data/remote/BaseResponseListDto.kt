package com.example.shoestoreapp.features.invoice.data.remote

import com.google.gson.annotations.SerializedName

data class BaseResponseListDto<T>(
    @SerializedName("message")
    val message: String?,

    @SerializedName("data")
    val data: T?
)