package com.example.shoestoreapp.features.agent_intelligent.data.remote

import com.google.gson.annotations.SerializedName

data class ChatSessionResponseDto (
    @SerializedName("publicId")
    val publicId : String?  ,
    @SerializedName("publicUserId")
    val publicUserId : String?,
    @SerializedName("title")
    val title : String?,
    @SerializedName("createAt")
    val createdAt : String,
)