package com.example.shoestoreapp.features.agent_intelligent.viewmodel

import com.example.shoestoreapp.core.utils.JwtUtils.removeHiddenAiTags
import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val isStreaming: Boolean = false, // Determines if the typing cursor '|'
    val createdAt : String? = "Now"
){
    val displayText : String
        get() = text.removeHiddenAiTags()
}