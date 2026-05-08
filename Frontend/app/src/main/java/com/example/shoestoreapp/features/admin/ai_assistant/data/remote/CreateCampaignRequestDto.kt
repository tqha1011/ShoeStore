package com.example.shoestoreapp.features.admin.ai_assistant.data.remote

import com.google.gson.annotations.SerializedName

data class CreateCampaignRequestDto (
    @SerializedName("PublicSessionId")
    val publicSessionId : String,
    @SerializedName("Content")
    val content : String,
)