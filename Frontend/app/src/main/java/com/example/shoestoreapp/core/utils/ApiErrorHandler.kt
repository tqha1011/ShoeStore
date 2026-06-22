package com.example.shoestoreapp.core.utils
import org.json.JSONObject
import retrofit2.Response

data class ApiErrorDetails(
    val code: String? = null,
    val message: String? = null
)

object ApiErrorHandler {

    fun extractErrorMessage(response: Response<*>): String? {
        val rawMessage = response.errorBody()?.string()?.takeIf { it.isNotBlank() }
        return parseBackendError(rawMessage).message
    }

    fun extractErrorDetails(response: Response<*>): ApiErrorDetails {
        val rawMessage = response.errorBody()?.string()?.takeIf { it.isNotBlank() }
        return parseBackendError(rawMessage)
    }

    // Logic bóc tách JSON
    private fun parseBackendError(rawMessage: String?): ApiErrorDetails {
        if (rawMessage.isNullOrBlank()) return ApiErrorDetails()
        return try {
            val jsonObject = JSONObject(rawMessage)
            val code = jsonObject.optString("code").takeIf { it.isNotBlank() }

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
                if (errorMessages.isNotEmpty()) {
                    return ApiErrorDetails(code = code, message = errorMessages.joinToString("\n"))
                }
            }

            // 2. Quét key "detail"
            if (jsonObject.has("detail")) {
                return ApiErrorDetails(code = code, message = jsonObject.getString("detail"))
            }

            // 3. Quét key "message"
            if (jsonObject.has("message")) {
                return ApiErrorDetails(code = code, message = jsonObject.getString("message"))
            }

            // 4. Quét key "title"
            if (jsonObject.has("title")) {
                return ApiErrorDetails(code = code, message = jsonObject.getString("title"))
            }

            ApiErrorDetails(code = code, message = rawMessage)
        } catch (_: Exception) {
            ApiErrorDetails(message = rawMessage)
        }
    }
}
