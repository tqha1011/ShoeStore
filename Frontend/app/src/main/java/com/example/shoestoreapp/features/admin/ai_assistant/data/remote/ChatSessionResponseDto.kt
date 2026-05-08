package com.example.shoestoreapp.features.admin.ai_assistant.data.remote

import com.google.gson.annotations.SerializedName

data class ChatSessionResponseDto (
    @SerializedName("PublicId")
    val PublicId : String,
    @SerializedName("PublicUserId")
    val PublicUserId : String,
    @SerializedName("Title")
    val Title : String?,
    @SerializedName("CreateAt")
    val CreateAt : String,
)