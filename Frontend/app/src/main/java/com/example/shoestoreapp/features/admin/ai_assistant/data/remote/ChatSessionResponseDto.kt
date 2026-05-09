package com.example.shoestoreapp.features.admin.ai_assistant.data.remote

import com.google.gson.annotations.SerializedName

data class ChatSessionResponseDto (
    @SerializedName("PublicId")
    val publicId : String,
    @SerializedName("PublicUserId")
    val publicUserId : String,
    @SerializedName("Title")
    val title : String?,
    @SerializedName("CreateAt")
    val createAt : String,
)