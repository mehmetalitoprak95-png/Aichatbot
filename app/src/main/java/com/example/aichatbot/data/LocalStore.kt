package com.example.aichatbot.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

private const val PREFS_NAME = "torqai_settings"
private const val KEY_BASE_URL = "base_url"
private const val KEY_API_KEY = "api_key"
private const val KEY_CHAT_MODEL = "model"
private const val KEY_IMAGE_MODEL = "image_model"
private const val KEY_THEME_MODE = "theme_mode"
private const val CONVERSATIONS_FILE = "conversations.json"
private const val HISTORY_FILE = "chat_history.json"

data class Conversation(
    val id: String,
    val title: String,
    val updatedAt: Long,
    val messages: List<ChatMessage>
)

const val DEFAULT_CHAT_MODEL = "openrouter/free"
const val DEFAULT_IMAGE_MODEL = "google/gemini-2.5-flash-image"

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class AppSettings(
    val baseUrl: String,
    val apiKey: String,
    val chatModel: String,
    val imageModel: String
)

/**
 * Ayarlari (SharedPreferences) ve sohbet gecmisini (dosya) telefonda saklar,
 * boylece uygulama kapatilip acildiginda her sey oldugu gibi kalir.
 */
object LocalStore {

    fun loadSettings(context: Context): AppSettings {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return AppSettings(
            baseUrl = prefs.getString(KEY_BASE_URL, "") ?: "",
            apiKey = prefs.getString(KEY_API_KEY, "") ?: "",
            chatModel = prefs.getString(KEY_CHAT_MODEL, "") ?: "",
            imageModel = prefs.getString(KEY_IMAGE_MODEL, "") ?: ""
        )
    }

    fun saveSettings(context: Context, baseUrl: String, apiKey: String, chatModel: String, imageModel: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_BASE_URL, baseUrl)
            .putString(KEY_API_KEY, apiKey)
            .putString(KEY_CHAT_MODEL, chatModel)
            .putString(KEY_IMAGE_MODEL, imageModel)
            .apply()
    }

    fun loadThemeMode(context: Context): ThemeMode {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
        return try {
            ThemeMode.valueOf(raw ?: ThemeMode.SYSTEM.name)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }

    fun saveThemeMode(context: Context, mode: ThemeMode) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_THEME_MODE, mode.name)
            .apply()
    }

    suspend fun loadHistory(context: Context): List<ChatMessage> = withContext(Dispatchers.IO) {
        try {
            val file = File(context.filesDir, HISTORY_FILE)
            if (!file.exists()) return@withContext emptyList()
            val arr = JSONArray(file.readText())
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                ChatMessage(
                    role = o.getString("role"),
                    text = o.optString("text", ""),
                    imageDataUrl = o.optString("imageDataUrl", "").ifBlank { null }
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveHistory(context: Context, messages: List<ChatMessage>) = withContext(Dispatchers.IO) {
        try {
            val arr = JSONArray()
            messages.forEach { m ->
                arr.put(
                    JSONObject()
                        .put("role", m.role)
                        .put("text", m.text)
                        .put("imageDataUrl", m.imageDataUrl ?: "")
                )
            }
            File(context.filesDir, HISTORY_FILE).writeText(arr.toString())
        } catch (e: Exception) {
            // Kaydedilemezse sessizce gec, sohbet yine de devam etsin
        }
    }

    suspend fun clearHistory(context: Context) = withContext(Dispatchers.IO) {
        try {
            File(context.filesDir, HISTORY_FILE).delete()
        } catch (e: Exception) {
        }
    }

    suspend fun loadConversations(context: Context): List<Conversation> = withContext(Dispatchers.IO) {
        try {
            val file = File(context.filesDir, CONVERSATIONS_FILE)
            if (!file.exists()) return@withContext emptyList()
            val arr = JSONArray(file.readText())
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                val msgsArr = o.getJSONArray("messages")
                val msgs = (0 until msgsArr.length()).map { j ->
                    val m = msgsArr.getJSONObject(j)
                    ChatMessage(
                        role = m.getString("role"),
                        text = m.optString("text", ""),
                        imageDataUrl = m.optString("imageDataUrl", "").ifBlank { null }
                    )
                }
                Conversation(
                    id = o.getString("id"),
                    title = o.getString("title"),
                    updatedAt = o.optLong("updatedAt", 0L),
                    messages = msgs
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveConversations(context: Context, conversations: List<Conversation>) = withContext(Dispatchers.IO) {
        try {
            val arr = JSONArray()
            conversations.forEach { c ->
                val msgsArr = JSONArray()
                c.messages.forEach { m ->
                    msgsArr.put(
                        JSONObject()
                            .put("role", m.role)
                            .put("text", m.text)
                            .put("imageDataUrl", m.imageDataUrl ?: "")
                    )
                }
                arr.put(
                    JSONObject()
                        .put("id", c.id)
                        .put("title", c.title)
                        .put("updatedAt", c.updatedAt)
                        .put("messages", msgsArr)
                )
            }
            File(context.filesDir, CONVERSATIONS_FILE).writeText(arr.toString())
        } catch (e: Exception) {
            // Kaydedilemezse sessizce gec
        }
    }
}
