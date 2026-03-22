package com.example.shoestoreapp.core.utils

import android.util.Base64
import org.json.JSONObject

object JwtUtils {
    fun getRoleFromToken(token: String): String {
        return try {
            // 1. Cắt token làm 3 khúc, lấy khúc giữa (index 1)
            val split = token.split(".")
            if (split.size < 2) return "USER" // An toàn trên hết

            // 2. Giải mã Base64 thành chuỗi JSON đọc được
            val payloadBytes = Base64.decode(split[1], Base64.URL_SAFE)
            val payloadString = String(payloadBytes, Charsets.UTF_8)

            // 3. Chuyển thành Object JSON để dễ moi móc
            val jsonObject = JSONObject(payloadString)

            // 4. Tìm cái Role. Đề phòng Backend C# .NET hay tự đổi tên key rườm rà
            when {
                jsonObject.has("role") -> jsonObject.getString("role")
                jsonObject.has("Role") -> jsonObject.getString("Role")
                // Cái dòng URL dài ngoằng này là "đặc sản" của C# .NET nhé
                jsonObject.has("http://schemas.microsoft.com/ws/2008/06/identity/claims/role") ->
                    jsonObject.getString("http://schemas.microsoft.com/ws/2008/06/identity/claims/role")
                else -> "USER" // Không tìm thấy thì gán mặc định là dân thường
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "USER"
        }
    }
}