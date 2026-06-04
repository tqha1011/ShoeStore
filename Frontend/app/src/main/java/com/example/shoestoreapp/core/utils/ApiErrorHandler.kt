package com.example.shoestoreapp.core.utils
import org.json.JSONObject
import retrofit2.Response

object ApiErrorHandler {

    fun extractErrorMessage(response: Response<*>): String? {
        val rawMessage = response.errorBody()?.string()?.takeIf { it.isNotBlank() }
        return parseBackendError(rawMessage)
    }

    // Logic bóc tách JSON
    private fun parseBackendError(rawMessage: String?): String? {
        if (rawMessage.isNullOrBlank()) return null
        return try {
            val jsonObject = JSONObject(rawMessage)

            // 1. Quét mảng "errors"
            if (jsonObject.has("errors")) {
                val errorsObj = jsonObject.getJSONObject("errors")
                val errorMessages = mutableListOf<String>()
                val keys = errorsObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val errorArray = errorsObj.getJSONArray(key)
                    for (i in 0 until errorArray.length()) {
                        errorMessages.add(errorArray.getString(i))
                    }
                }
                if (errorMessages.isNotEmpty()) return errorMessages.joinToString("\n")
            }

            // 2. Quét key "detail"
            if (jsonObject.has("detail")) return jsonObject.getString("detail")

            // 3. Quét key "message"
            if (jsonObject.has("message")) return jsonObject.getString("message")

            // 4. Quét key "title"
            if (jsonObject.has("title")) return jsonObject.getString("title")

            rawMessage
        } catch (_: Exception) {
            rawMessage
        }
    }
}