package com.example.floatinggemini

import android.graphics.Bitmap
import android.util.Base64
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException

object GeminiApiClient {

    private const val API_KEY = "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"

    private const val URL = "https://openrouter.ai/api/v1/chat/completions"

    // 🤖 GANTI MODEL DI SINI AJA KALAU MAU GANTI
    private const val MODEL = "thinkingmachines/inkling-small:free"

    fun analyzeImage(bitmap: Bitmap, prompt: String, callback: (String) -> Unit) {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

        val jsonPayload = JSONObject().apply {
            put("model", MODEL)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", JSONArray().apply {
                        put(JSONObject().apply {
                            put("type", "text")
                            put("text", prompt)
                        })
                        put(JSONObject().apply {
                            put("type", "image_url")
                            put("image_url", JSONObject().apply {
                                put("url", "data:image/jpeg;base64,$base64Image")
                            })
                        })
                    })
                })
            })
        }

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(URL)
            .addHeader("Authorization", "Bearer $API_KEY")
            .addHeader("Content-Type", "application/json")
            .addHeader("HTTP-Referer", "https://floatinggemini.app")
            .addHeader("X-Title", "Floating Gemini")
            .post(jsonPayload.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback("Gagal koneksi: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: "Empty response"
                try {
                    val jsonResponse = JSONObject(body)
                    val text = jsonResponse
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")
                    callback(text)
                } catch (e: Exception) {
                    callback("Error parsing: $body")
                }
            }
        })
    }
}
