package com.example.aichatbot.data

data class ChatMessage(
    val role: String,  // "user" veya "assistant"
    val text: String
) {
    val isUser: Boolean get() = role == "user"
}
