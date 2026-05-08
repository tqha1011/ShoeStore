package com.example.shoestoreapp.features.admin.ai_assistant.viewmodel

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val isStreaming: Boolean = false // Determines if the typing cursor '█'
)