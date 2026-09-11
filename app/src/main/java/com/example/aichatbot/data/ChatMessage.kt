package com.example.aichatbot.data

data class ChatMessage(
    val role: String,  // "user" veya "assistant"
    val text: String,
    val imageDataUrl: String? = null  // data:image/...;base64,... (eklenen ya da uretilen gorsel)
) {
    val isUser: Boolean get() = role == "user"
}
