package com.example.aichatbot.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

object ChatApi {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .build()

    private val jsonMedia = "application/json; charset=utf-8".toMediaType()

    /**
     * OpenAI uyumlu herhangi bir /chat/completions uç noktasıyla konuşur.
     * Varsayılan: https://api.openai.com  — kendi backend proxy'ini de yazabilirsin.
     */
    suspend fun sendMessage(
        baseUrl: String,
        apiKey: String,
        model: String,
        history: List<ChatMessage>
    ): String = withContext(Dispatchers.IO) {
        val payload = JSONObject()
            .put("model", model)
            .put("max_tokens", 800)
            .put("messages", JSONArray().apply {
                history.forEach { msg ->
                    put(JSONObject().put("role", msg.role).put("content", msg.text))
                }
            })

        val request = Request.Builder()
            .url(buildEndpoint(baseUrl))
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .post(payload.toString().toRequestBody(jsonMedia))
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw IOException("HTTP ${response.code}: $body")
            JSONObject(body)
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim()
        }
    }

    /**
     * Girilen taban URL'den istek ucu belirler:
     * - https://api.openai.com                                  -> /v1/chat/completions
     * - https://openrouter.ai/api                               -> /api/v1/chat/completions
     * - https://generativelanguage.googleapis.com/v1beta/openai -> /chat/completions (Gemini)
     * - tam yol verilmişse (…/chat/completions)                 -> olduğu gibi
     */
    private fun buildEndpoint(baseUrl: String): String {
        val trimmed = baseUrl.trimEnd('/')
        return when {
            trimmed.endsWith("/chat/completions") -> trimmed
            trimmed.endsWith("/openai") -> trimmed + "/chat/completions"
            else -> trimmed + "/v1/chat/completions"
        }
    }
}
