package com.example.ai

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiBrain {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun askAssistant(userPrompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getOfflineSmartResponse(userPrompt)
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        try {
            val systemInstruction = "You are Aura, an intelligent, helpful Android AI voice assistant. Give concise, direct, spoken-style responses suitable for Text-to-Speech (1-3 sentences max). Be friendly, accurate, and avoid formatting characters like markdown asterisks or bullet points."

            val jsonBody = JSONObject().apply {
                val contents = JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", userPrompt))
                        })
                    })
                }
                put("contents", contents)

                val sysContent = JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", systemInstruction))
                    })
                }
                put("systemInstruction", sysContent)
            }

            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful && responseBody.isNotBlank()) {
                val json = JSONObject(responseBody)
                val candidates = json.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val text = parts.getJSONObject(0).optString("text", "")
                        if (text.isNotBlank()) {
                            return@withContext cleanMarkdown(text)
                        }
                    }
                }
            }
            return@withContext getOfflineSmartResponse(userPrompt)
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext getOfflineSmartResponse(userPrompt)
        }
    }

    private fun cleanMarkdown(text: String): String {
        return text.replace(Regex("[*#_`~]"), "").trim()
    }

    private fun getOfflineSmartResponse(prompt: String): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("weather") || lower.contains("mausam") ->
                "The current local weather is mild and clear with comfortable conditions. You can check detailed forecasts in your weather app."
            lower.contains("who are you") || lower.contains("tum kaun ho") ->
                "I am Aura, your personal AI voice assistant on Android. I can make calls, send messages, adjust volume, launch apps, and manage your device."
            lower.contains("how are you") || lower.contains("kaise ho") ->
                "I'm running at peak performance and ready to help you with your phone!"
            lower.contains("joke") || lower.contains("chutkula") ->
                "Why don't scientists trust atoms? Because they make up everything!"
            lower.contains("thank") || lower.contains("shukriya") ->
                "You're very welcome! Let me know if you need anything else."
            lower.contains("help") || lower.contains("kya kar sakte") ->
                "You can tell me to call someone, send a WhatsApp message, increase volume, open YouTube, set an alarm, or search Google."
            else ->
                "I understood your request: '$prompt'. I am ready to assist with any phone controls or voice actions."
        }
    }
}
