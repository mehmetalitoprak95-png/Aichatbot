package com.example.aichatbot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.aichatbot.ui.ChatScreen
import com.example.aichatbot.ui.theme.AIChatBotTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AIChatBotTheme {
                ChatScreen()
            }
        }
    }
}
