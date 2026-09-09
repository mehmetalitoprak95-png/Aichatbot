package com.example.aichatbot.ui

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aichatbot.data.ChatApi
import com.example.aichatbot.data.ChatMessage
import kotlinx.coroutines.launch

object SettingsStore {
    private const val PREFS = "chat_settings"

    fun load(context: Context): Triple<String, String, String> {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return Triple(
            p.getString("base_url", "https://api.openai.com") ?: "https://api.openai.com",
            p.getString("api_key", "") ?: "",
            p.getString("model", "gpt-4o-mini") ?: "gpt-4o-mini"
        )
    }

    fun save(context: Context, baseUrl: String, apiKey: String, model: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString("base_url", baseUrl)
            .putString("api_key", apiKey)
            .putString("model", model)
            .apply()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var messages by remember {
        mutableStateOf(
            listOf(ChatMessage("assistant", "Merhaba! Ben senin yapay zekâ asistanınim. Bana bir şey sorabilirsin 🙂"))
        )
    }
    var input by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showFlagDialog by remember { mutableStateOf(false) }
    var flaggedMessage by remember { mutableStateOf<ChatMessage?>(null) }

    var baseUrl by remember { mutableStateOf("https://api.openai.com") }
    var apiKey by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("gpt-4o-mini") }

    LaunchedEffect(Unit) {
        val (u, k, m) = SettingsStore.load(context)
        baseUrl = u
        apiKey = k
        model = m
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Sohbet Asistanı") },
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Ayarlar")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {

            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { msg ->
                    MessageBubble(
                        msg = msg,
                        onLongClick = {
                            if (!msg.isUser) {
                                flaggedMessage = msg
                                showFlagDialog = true
                            }
                        }
                    )
                }
                if (loading) item { LoadingBubble() }
            }

            Row(
                Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Mesajını yaz…") },
                    enabled = !loading,
                    maxLines = 4
                )
                Spacer(Modifier.width(8.dp))
                FloatingActionButton(
                    onClick = {
                        val text = input.trim()
                        if (text.isEmpty() || loading) return@FloatingActionButton
                        input = ""
                        messages = messages + ChatMessage("user", text)
                        loading = true
                        scope.launch {
                            try {
                                val reply = ChatApi.sendMessage(baseUrl, apiKey, model, messages)
                                messages = messages + ChatMessage("assistant", reply)
                            } catch (e: Exception) {
                                val hint = if (apiKey.isBlank()) "Önce ayarlardan API anahtarını gir."
                                else "Hata: ${e.message ?: "bilinmeyen hata"}"
                                messages = messages + ChatMessage("assistant", "Bir şeyler ters gitti. $hint")
                            } finally {
                                loading = false
                            }
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    if (!loading) {
                        Icon(Icons.Default.Send, contentDescription = "Gönder")
                    } else {
                        CircularProgressIndicator(
                            Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    }
                }
            }
        }
    }

    if (showSettings) {
        SettingsDialog(
            baseUrl = baseUrl,
            apiKey = apiKey,
            model = model,
            onDismiss = { showSettings = false },
            onSave = { b, k, m ->
                baseUrl = b
                apiKey = k
                model = m
                SettingsStore.save(context, b, k, m)
                showSettings = false
            }
        )
    }

    if (showFlagDialog) {
        AlertDialog(
            onDismissRequest = { showFlagDialog = false },
            title = { Text("Yanıtı raporla") },
            text = {
                Text(
                    "Bu yanıtı uygunsuz olarak bildirmek istiyor musun?\n" +
                        "(Play Store AI içerik politikası uygulama içi raporlama ister — " +
                        "yayın öncesinde bu akışı kendi raporlama sistemine bağla.)"
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    // TODO(yayın): kaydı kendi backend'ine gönder (örn. POST /report)
                    flaggedMessage = null
                    showFlagDialog = false
                }) { Text("Raporla") }
            },
            dismissButton = {
                TextButton(onClick = { showFlagDialog = false }) { Text("Vazgeç") }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageBubble(msg: ChatMessage, onLongClick: () -> Unit) {
    val isUser = msg.isUser
    Box(
        Modifier.fillMaxWidth(),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .combinedClickable(onClick = {}, onLongClick = onLongClick),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(msg.text, Modifier.padding(12.dp))
        }
    }
}

@Composable
private fun LoadingBubble() {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            CircularProgressIndicator(
                Modifier.padding(14.dp).size(22.dp),
                strokeWidth = 2.dp
            )
        }
    }
}

@Composable
private fun SettingsDialog(
    baseUrl: String,
    apiKey: String,
    model: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var b by remember { mutableStateOf(baseUrl) }
    var k by remember { mutableStateOf(apiKey) }
    var m by remember { mutableStateOf(model) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ayarlar") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = b,
                    onValueChange = { b = it },
                    label = { Text("API Taban URL") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = k,
                    onValueChange = { k = it },
                    label = { Text("API Anahtarı") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = m,
                    onValueChange = { m = it },
                    label = { Text("Model (ör. gpt-4o-mini)") },
                    singleLine = true
                )
                Text(
                    "Anahtar yalnızca bu cihazda saklanır. " +
                        "Yayın için bir backend proxy kullanman önerilir.",
                    fontSize = 12.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(b.trim(), k.trim(), m.trim()) }) { Text("Kaydet") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Vazgeç") }
        }
    )
}
