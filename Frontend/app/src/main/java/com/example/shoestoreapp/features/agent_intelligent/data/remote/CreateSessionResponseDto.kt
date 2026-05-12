package com.example.shoestoreapp.features.agent_intelligent.data.remote

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