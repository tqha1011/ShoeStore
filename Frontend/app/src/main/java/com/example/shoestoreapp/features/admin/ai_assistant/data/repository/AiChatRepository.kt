package com.example.shoestoreapp.features.admin.ai_assistant.data.repository

import com.example.shoestoreapp.core.utils.Constants
import com.example.shoestoreapp.features.admin.ai_assistant.data.remote.ChatSessionApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

class AiChatRepository (
    private val sessionApi : ChatSessionApi,
    private val okHttpClient: OkHttpClient
) {
    suspend fun createSession() = sessionApi.createSession()
    suspend fun getSessions(pageNumber : Int, pageSize : Int) = sessionApi.getSession(pageNumber, pageSize)

    fun streamChatStatistics(sessionId : String, title : String) =
        baseStreamCall("${Constants.BASE_URL}/api/v1/chatbot/chat-statistics?publicSessionId=$sessionId", title)

    fun streamGenerateCampaign(sessionId : String, content : String) =
        baseStreamCall("${Constants.BASE_URL}/api/v1/chatbot/generate-campaign", content, sessionId)

    private fun baseStreamCall(url : String, content : String, sessionId : String? = null) : Flow<String> = flow {
        val json = JSONObject().apply {
            put("Content", content)
            if (sessionId != null) put("PublicSessionId", sessionId)
        }.toString()

        val body = json.toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("Accept", "text/event-stream")
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Stream API Error: ${response.code}")

            val responseBody = response.body ?: throw Exception("Stream API Error: empty response body")
            val reader = BufferedReader(InputStreamReader(responseBody.byteStream()))
            var line : String?
            while (reader.readLine().also { line = it } != null) {
               if ( line!!.startsWith("data: ")){
                   val chunk = line.substring(6)
                   if (chunk.trim() == "[DONE]") break
                   emit(if (chunk.isBlank()) "\n" else chunk)
               }
            }
        }
    }
}
