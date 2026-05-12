package com.example.shoestoreapp.features.admin.ai_assistant.data.remote

import com.google.gson.annotations.SerializedName

data class CreateSessionResponseDto(
    @SerializedName(
        value = "PublicSessionId",
        alternate = ["publicSessionId", "public_session_id", "publicId", "PublicId", "id", "sessionId"]
    )
    val publicSessionId : String?,
    @SerializedName(value = "Title", alternate = ["title"])
    val title : String?,
)