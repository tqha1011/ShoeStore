package com.example.shoestoreapp.features.agent_intelligent.data.remote

import com.google.gson.annotations.SerializedName
data class ChatHistoryResponseDto (
    @SerializedName("messages")
    val messages: List<ChatMessageDto>,

    @SerializedName("nextCursor")
    val nextCursor: String?
)

data class ChatMessageDto (
    @SerializedName("messageId")
    val messageId: String?,

    @SerializedName("content")
    val content: String?,

    @SerializedName("role")
    val role: String?,

    @SerializedName("createdAt")
    val createdAt : String?
)

