package com.example.shoestoreapp.features.admin.ai_assistant.data.remote

import com.google.gson.annotations.SerializedName

data class CreateSessionResponseDto(
    @SerializedName("PublicSessionId")
    val publicSessionId : String,
    @SerializedName("Title")
    val title : String,
)