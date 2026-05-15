package com.example.shoestoreapp.features.agent_intelligent.viewmodel

import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val isStreaming: Boolean = false // Determines if the typing cursor '|'
)