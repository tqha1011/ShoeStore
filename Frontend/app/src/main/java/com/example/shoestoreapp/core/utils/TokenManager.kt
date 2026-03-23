package com.example.shoestoreapp.core.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Initialize DataStore (local storage). File name: "session_prefs.preferences_pb"
private val Context.dataStore by preferencesDataStore(name = "session_prefs")

class TokenManager(private val context: Context) {

    // Keys for storing token and role
    companion object {
        val TOKEN_KEY = stringPreferencesKey("jwt_token")
        val ROLE_KEY = stringPreferencesKey("user_role")
    }

    /**
     * Save auth info (token + role) after successful login
     */
    suspend fun saveAuthInfo(token: String, role: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[ROLE_KEY] = role
        }
    }

    /**
     * Get JWT token as Flow
     * Used by AuthInterceptor to attach token to API requests
     */
    val getToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[TOKEN_KEY]
    }

    /**
     * Get user role as Flow
     * Used by UI to control access (ADMIN / USER)
     */
    val getRole: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[ROLE_KEY]
    }

    /**
     * Clear auth data (logout)
     */
    suspend fun clearAuthInfo() {
        context.dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(ROLE_KEY)
        }
    }
}