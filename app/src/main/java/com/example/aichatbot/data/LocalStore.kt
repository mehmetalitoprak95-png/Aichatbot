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
private const val KEY_MODEL = "model"
private const val KEY_THEME_MODE = "theme_mode"
private const val HISTORY_FILE = "chat_history.json"

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/**
 * Ayarlari (SharedPreferences) ve sohbet gecmisini (dosya) telefonda saklar,
 * boylece uygulama kapatilip acildiginda her sey oldugu gibi kalir.
 */
object LocalStore {

    fun loadSettings(context: Context): Triple<String, String, String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return Triple(
            prefs.getString(KEY_BASE_URL, "") ?: "",
            prefs.getString(KEY_API_KEY, "") ?: "",
            prefs.getString(KEY_MODEL, "") ?: ""
        )
    }

    fun saveSettings(context: Context, baseUrl: String, apiKey: String, model: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_BASE_URL, baseUrl)
            .putString(KEY_API_KEY, apiKey)
            .putString(KEY_MODEL, model)
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
}
