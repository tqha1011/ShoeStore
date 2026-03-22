package com.example.shoestoreapp.core.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Khởi tạo DataStore (Cái két sắt). Tên file lưu dưới local điện thoại sẽ là "session_prefs.preferences_pb"
private val Context.dataStore by preferencesDataStore(name = "session_prefs")

class TokenManager(private val context: Context) {

    // Tạo các ổ khóa để cất và lấy đồ
    companion object {
        val TOKEN_KEY = stringPreferencesKey("jwt_token")
        val ROLE_KEY = stringPreferencesKey("user_role")
    }

    /**
     * 1. HÀM CẤT DỮ LIỆU
     * Dùng khi user Đăng nhập thành công. Cất cả Token và Role vào két.
     */
    suspend fun saveAuthInfo(token: String, role: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[ROLE_KEY] = role
        }
    }

    /**
     * 2. HÀM LẤY TOKEN
     * Trả về một Flow. Lát nữa thằng AuthInterceptor sẽ gọi cái này để lấy Token đính kèm vào API.
     */
    val getToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[TOKEN_KEY]
    }

    /**
     * 3. HÀM LẤY ROLE
     * Trả về Flow. Các màn hình UI sẽ gọi cái này để biết là ADMIN hay USER mà ẩn/hiện nút bấm.
     */
    val getRole: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[ROLE_KEY]
    }

    /**
     * 4. HÀM XÓA DỮ LIỆU (ĐĂNG XUẤT)
     * Dùng khi user bấm nút "Logout". Quét sạch két sắt để văng ra màn hình Login.
     */
    suspend fun clearAuthInfo() {
        context.dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(ROLE_KEY)
        }
    }
}