package com.example.shoestoreapp.features.agent_intelligent.data.remote

import com.google.gson.annotations.SerializedName

data class ChatMessageRequestDto (
    @SerializedName("content")
    val content : String
)