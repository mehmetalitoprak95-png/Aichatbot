package com.example.aichatbot.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aichatbot.R
import com.example.aichatbot.data.ChatApi
import com.example.aichatbot.data.ChatMessage
import com.example.aichatbot.data.LocalStore
import com.example.aichatbot.data.ThemeMode
import com.example.aichatbot.ui.theme.AIChatBotTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    var themeMode by remember { mutableStateOf(LocalStore.loadThemeMode(context)) }
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val savedSettings = remember { LocalStore.loadSettings(context) }
    var baseUrl by remember { mutableStateOf(savedSettings.first) }
    var apiKey by remember { mutableStateOf(savedSettings.second) }
    var model by remember { mutableStateOf(savedSettings.third) }
    var showSettings by remember { mutableStateOf(false) }

    val welcomeMessage = ChatMessage("assistant", "Merhaba! Ben TORQ Ai. Nasıl yardımcı olabilirim? 🙂")
    var messages by remember { mutableStateOf(listOf(welcomeMessage)) }
    var historyLoaded by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var selectedImage by remember { mutableStateOf<String?>(null) }
    var imageGenMode by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                selectedImage = downscaleAndEncode(context, uri)
            }
        }
    }

    // Kaydedilmis sohbet gecmisini bir kere yukle (varsa)
    LaunchedEffect(Unit) {
        val saved = LocalStore.loadHistory(context)
        if (saved.isNotEmpty()) messages = saved
        historyLoaded = true
    }

    // Sohbet her degistiginde diske kaydet (ilk yukleme bitmeden kaydetme, uzerine yazmasin)
    LaunchedEffect(messages) {
        if (historyLoaded) LocalStore.saveHistory(context, messages)
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    fun startNewChat() {
        messages = listOf(welcomeMessage)
        scope.launch { LocalStore.clearHistory(context) }
        scope.launch { drawerState.close() }
    }

    fun changeTheme(mode: ThemeMode) {
        themeMode = mode
        LocalStore.saveThemeMode(context, mode)
    }

    AIChatBotTheme(darkTheme = darkTheme) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Text(
                        stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(20.dp)
                    )
                    HorizontalDivider()
                    NavigationDrawerItem(
                        label = { Text("Yeni Sohbet") },
                        icon = { Icon(Icons.Default.AddComment, contentDescription = null) },
                        selected = false,
                        onClick = { startNewChat() },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    NavigationDrawerItem(
                        label = { Text("Ayarlar") },
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        selected = false,
                        onClick = {
                            showSettings = true
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    Text(
                        "GÖRÜNÜM",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 28.dp)
                    )
                    NavigationDrawerItem(
                        label = { Text("Sistem") },
                        icon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null) },
                        selected = themeMode == ThemeMode.SYSTEM,
                        onClick = { changeTheme(ThemeMode.SYSTEM) },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    NavigationDrawerItem(
                        label = { Text("Açık") },
                        icon = { Icon(Icons.Default.LightMode, contentDescription = null) },
                        selected = themeMode == ThemeMode.LIGHT,
                        onClick = { changeTheme(ThemeMode.LIGHT) },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    NavigationDrawerItem(
                        label = { Text("Koyu") },
                        icon = { Icon(Icons.Default.DarkMode, contentDescription = null) },
                        selected = themeMode == ThemeMode.DARK,
                        onClick = { changeTheme(ThemeMode.DARK) },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menü")
                            }
                        },
                        title = {
                            Text(
                                stringResource(R.string.app_name),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    )
                }
            ) { padding ->
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(messages) { msg -> MessageBubble(msg) }
                        if (loading) {
                            item {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Düşünüyor…", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    if (selectedImage != null) {
                        val previewBitmap = remember(selectedImage) { decodeDataUrlToBitmap(selectedImage!!) }
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            previewBitmap?.let {
                                Image(
                                    bitmap = it.asImageBitmap(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text("Görsel eklendi", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.weight(1f))
                            IconButton(onClick = { selectedImage = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Görseli kaldır")
                            }
                        }
                    }

                    if (imageGenMode) {
                        Text(
                            "✨ Görsel üretim modu açık",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(28.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = {
                                imagePicker.launch(
                                    PickVisualMediaRequest(mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }) {
                                Icon(Icons.Default.AttachFile, contentDescription = "Görsel ekle")
                            }
                            IconButton(onClick = { imageGenMode = !imageGenMode }) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = "Görsel üret modu",
                                    tint = if (imageGenMode) MaterialTheme.colorScheme.tertiary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            OutlinedTextField(
                                value = input,
                                onValueChange = { input = it },
                                modifier = Modifier.weight(1f),
                                placeholder = {
                                    Text(if (imageGenMode) "Görsel için bir şey tarif et…" else "Mesajını yaz…")
                                },
                                enabled = !loading,
                                maxLines = 4,
                                shape = RoundedCornerShape(20.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent
                                )
                            )
                            Spacer(Modifier.width(4.dp))
                            FloatingActionButton(
                                modifier = Modifier.size(46.dp),
                                onClick = {
                                    val text = input.trim()
                                    if ((text.isEmpty() && selectedImage == null) || loading) return@FloatingActionButton

                                    val outgoing = ChatMessage("user", text, selectedImage)
                                    val wantImage = imageGenMode
                                    input = ""
                                    selectedImage = null
                                    messages = messages + outgoing
                                    loading = true

                                    scope.launch {
                                        try {
                                            val result = ChatApi.sendMessage(baseUrl, apiKey, model, messages, generateImage = wantImage)
                                            messages = messages + ChatMessage(
                                                "assistant",
                                                result.text ?: if (result.imageDataUrl != null) "" else "(boş cevap)",
                                                result.imageDataUrl
                                            )
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
                                        Modifier.size(20.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                }
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
                        baseUrl = b; apiKey = k; model = m
                        LocalStore.saveSettings(context, b, k, m)
                        showSettings = false
                    }
                )
            }
        }
    }
}

@Composable
private fun MessageBubble(msg: ChatMessage) {
    val isUser = msg.isUser
    val bitmap = remember(msg.imageDataUrl) { msg.imageDataUrl?.let { decodeDataUrlToBitmap(it) } }

    Box(
        Modifier.fillMaxWidth(),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (isUser) 20.dp else 6.dp,
                bottomEnd = if (isUser) 6.dp else 20.dp
            ),
            color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 1.dp
        ) {
            Column(Modifier.padding(if (bitmap != null) 6.dp else 12.dp)) {
                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                    )
                    if (msg.text.isNotBlank()) Spacer(Modifier.height(8.dp))
                }
                if (msg.text.isNotBlank()) {
                    Text(
                        msg.text,
                        modifier = if (bitmap != null) Modifier.padding(horizontal = 6.dp, vertical = 4.dp) else Modifier,
                        color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
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
            Column {
                OutlinedTextField(
                    value = b, onValueChange = { b = it },
                    label = { Text("API Taban URL") },
                    placeholder = { Text("https://openrouter.ai/api") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = k, onValueChange = { k = it },
                    label = { Text("API Anahtarı") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = m, onValueChange = { m = it },
                    label = { Text("Model") },
                    placeholder = { Text("openrouter/free") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Görsel üretmek için modeli görsel destekleyen bir modelle değiştir " +
                        "(ör. google/gemini-2.5-flash-image-preview). Bu modeller genelde ücretlidir.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(b.trim(), k.trim(), m.trim()) }) { Text("Kaydet") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )
}

private suspend fun downscaleAndEncode(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
    try {
        context.contentResolver.openInputStream(uri)?.use { input ->
            val original = BitmapFactory.decodeStream(input) ?: return@withContext null
            val maxDim = 1024
            val largestSide = maxOf(original.width, original.height)
            val scale = if (largestSide > maxDim) maxDim.toFloat() / largestSide else 1f
            val scaled = if (scale < 1f) {
                Bitmap.createScaledBitmap(
                    original,
                    (original.width * scale).toInt().coerceAtLeast(1),
                    (original.height * scale).toInt().coerceAtLeast(1),
                    true
                )
            } else original
            val baos = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, 82, baos)
            "data:image/jpeg;base64," + Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
        }
    } catch (e: Exception) {
        null
    }
}

private fun decodeDataUrlToBitmap(dataUrl: String): Bitmap? {
    return try {
        val base64Part = dataUrl.substringAfter(",", "")
        if (base64Part.isBlank()) return null
        val bytes = Base64.decode(base64Part, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    } catch (e: Exception) {
        null
    }
}
