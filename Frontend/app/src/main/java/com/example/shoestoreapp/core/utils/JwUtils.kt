package com.example.shoestoreapp.core.utils

import android.util.Base64
import org.json.JSONObject
object JwtUtils {

    fun normalizeToken(token: String?): String? {
        if (token.isNullOrBlank()) return null
        return token.trim()
            .removePrefix("Bearer ")
            .removePrefix("bearer ")
            .trim()
            .ifBlank { null }
    }

    fun getRoleFromToken(token: String): String {
        return try {
            val normalizedToken = normalizeToken(token) ?: return "USER"

            // 1. Split JWT into 3 parts, get payload (index 1)
            val split = normalizedToken.split(".")
            if (split.size < 2) return "USER" // Fallback for invalid token

            // 2. Decode Base64 payload to JSON string
            val payloadBytes = Base64.decode(split[1], Base64.URL_SAFE)
            val payloadString = String(payloadBytes, Charsets.UTF_8)

            // 3. Parse payload to JSON object
            val jsonObject = JSONObject(payloadString)

            // 4. Extract role (handle different backend naming conventions)
            when {
                jsonObject.has("role") -> jsonObject.getString("role")
                jsonObject.has("Role") -> jsonObject.getString("Role")
                // Common claim format in C# .NET
                jsonObject.has("http://schemas.microsoft.com/ws/2008/06/identity/claims/role") ->
                    jsonObject.getString("http://schemas.microsoft.com/ws/2008/06/identity/claims/role")
                else -> "USER" // Default fallback
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "USER" // Return default role on error
        }
    }

    fun isTokenExpired(token: String?): Boolean {
        return try {
            val normalizedToken = normalizeToken(token) ?: return true
            val split = normalizedToken.split(".")
            if (split.size < 2) return true

            val payloadBytes = Base64.decode(split[1], Base64.URL_SAFE)
            val payloadString = String(payloadBytes, Charsets.UTF_8)
            val jsonObject = JSONObject(payloadString)
            val expSeconds = jsonObject.optLong("exp", 0L)
            if (expSeconds <= 0L) return true

            val nowSeconds = System.currentTimeMillis() / 1000
            nowSeconds >= expSeconds
        } catch (_: Exception) {
            true
        }
    }
}