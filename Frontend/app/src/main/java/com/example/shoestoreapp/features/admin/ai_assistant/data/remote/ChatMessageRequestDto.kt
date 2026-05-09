package com.example.shoestoreapp.features.admin.ai_assistant.data.remote

import com.google.gson.annotations.SerializedName

data class ChatMessageRequestDto (
    @SerializedName("content")
    val content : String
)