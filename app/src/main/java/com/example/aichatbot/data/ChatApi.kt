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

/**
 * Bir /chat/completions cevabinin sonucu.
 * text: modelin yazi cevabi (varsa)
 * imageDataUrl: modelin urettigi gorsel (varsa) - data:image/...;base64,...
 */
data class ChatResult(
    val text: String?,
    val imageDataUrl: String?
)

object ChatApi {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)
        .build()

    private val jsonMedia = "application/json; charset=utf-8".toMediaType()

    /**
     * OpenAI uyumlu herhangi bir /chat/completions uc noktasiyla konusur.
     * Mesaj gecmisinde gorsel (imageDataUrl) varsa vision formatinda gonderir.
     * generateImage = true ise modalities=["image","text"] ekler
     * (bunun icin secili modelin gorsel uretimini desteklemesi gerekir, ornek:
     * google/gemini-2.5-flash-image-preview).
     */
    suspend fun sendMessage(
        baseUrl: String,
        apiKey: String,
        model: String,
        history: List<ChatMessage>,
        generateImage: Boolean = false
    ): ChatResult = withContext(Dispatchers.IO) {
        var lastError: Exception? = null
        repeat(2) { attempt ->
            try {
                return@withContext sendOnce(baseUrl, apiKey, model, history, generateImage)
            } catch (e: IOException) {
                // Baglanti kopmasi gibi gecici hatalarda bir kez daha dene
                lastError = e
                if (attempt == 0) Thread.sleep(700)
            }
        }
        throw lastError ?: IOException("Bilinmeyen ağ hatası")
    }

    private fun sendOnce(
        baseUrl: String,
        apiKey: String,
        model: String,
        history: List<ChatMessage>,
        generateImage: Boolean
    ): ChatResult {
        val payload = JSONObject()
            .put("model", model)
            .put("max_tokens", 1500)
            .put("messages", JSONArray().apply {
                history.forEach { msg -> put(toMessageJson(msg)) }
            })

        if (generateImage) {
            payload.put("modalities", JSONArray(listOf("image", "text")))
        }

        val request = Request.Builder()
            .url(buildEndpoint(baseUrl))
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .post(payload.toString().toRequestBody(jsonMedia))
            .build()

        return client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw IOException("HTTP ${response.code}: $body")

            val message = JSONObject(body)
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")

            val text = message.optString("content", "").trim().ifBlank { null }

            val imageUrl = message.optJSONArray("images")
                ?.takeIf { it.length() > 0 }
                ?.optJSONObject(0)
                ?.optJSONObject("image_url")
                ?.optString("url")
                ?.takeIf { it.isNotBlank() }

            ChatResult(text = text, imageDataUrl = imageUrl)
        }
    }

    private fun toMessageJson(msg: ChatMessage): JSONObject {
        val obj = JSONObject().put("role", msg.role)
        return if (msg.imageDataUrl != null) {
            val contentArray = JSONArray()
            if (msg.text.isNotBlank()) {
                contentArray.put(JSONObject().put("type", "text").put("text", msg.text))
            }
            contentArray.put(
                JSONObject().put("type", "image_url")
                    .put("image_url", JSONObject().put("url", msg.imageDataUrl))
            )
            obj.put("content", contentArray)
        } else {
            obj.put("content", msg.text)
        }
    }

    /**
     * Girilen taban URL'den istek ucu belirler:
     * - https://api.openai.com                                  -> /v1/chat/completions
     * - https://openrouter.ai/api                               -> /api/v1/chat/completions
     * - https://generativelanguage.googleapis.com/v1beta/openai -> /chat/completions (Gemini)
     * - tam yol verilmisse (.../chat/completions)                -> oldugu gibi
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
