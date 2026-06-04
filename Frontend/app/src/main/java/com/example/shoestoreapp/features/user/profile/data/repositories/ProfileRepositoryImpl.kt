package com.example.shoestoreapp.features.user.profile.data.repositories

import com.example.shoestoreapp.core.networks.RetrofitInstance
import com.example.shoestoreapp.features.user.profile.data.models.UserProfile
import com.example.shoestoreapp.features.user.profile.data.remote.ChangePasswordDto
import com.example.shoestoreapp.features.user.profile.data.remote.ProfileApi
import com.example.shoestoreapp.features.user.profile.data.remote.UpdateProfileDto
import com.example.shoestoreapp.features.user.profile.data.remote.toUserProfile
import org.json.JSONObject
import retrofit2.Response

class ProfileRepositoryImpl(
    private val api: ProfileApi = RetrofitInstance.profileApi
) : ProfileRepository {

    override suspend fun getUserProfile(): Result<UserProfile> {
        return try {
            val response = api.getUserProfile()
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it.toUserProfile()) }
                    ?: Result.failure(ProfileRepositoryException.Unknown("Empty response body."))
            } else {
                Result.failure(response.toRepositoryException())
            }
        } catch (e: Exception) {
            Result.failure(ProfileRepositoryException.Unknown(e.message ?: ProfileRepositoryException.ERROR_UNKNOWN))
        }
    }

    override suspend fun updateProfile(dto: UpdateProfileDto): Result<Unit> {
        return try {
            val response = api.updateProfile(dto)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(response.toRepositoryException())
            }
        } catch (e: Exception) {
            Result.failure(ProfileRepositoryException.Unknown(e.message ?: ProfileRepositoryException.ERROR_UNKNOWN))
        }
    }

    override suspend fun changePassword(dto: ChangePasswordDto): Result<Unit> {
        return try {
            val response = api.changePassword(dto)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(response.toRepositoryException())
            }
        } catch (e: Exception) {
            Result.failure(ProfileRepositoryException.Unknown(e.message ?: ProfileRepositoryException.ERROR_UNKNOWN))
        }
    }

    // ================
    // LOGIC PARSE LỖI
    // ================

    private fun <T> Response<T>.toRepositoryException(): ProfileRepositoryException {
        val rawMessage = errorBody()?.string()?.takeIf { it.isNotBlank() }
        val backendMessage = parseBackendError(rawMessage)

        return when (code()) {
            400 -> ProfileRepositoryException.BadRequest(backendMessage ?: ProfileRepositoryException.ERROR_BAD_REQUEST)
            401 -> ProfileRepositoryException.Unauthorized(backendMessage ?: ProfileRepositoryException.ERROR_UNAUTHORIZED)
            404 -> ProfileRepositoryException.NotFound(backendMessage ?: ProfileRepositoryException.ERROR_NOT_FOUND)
            500 -> ProfileRepositoryException.ServerError(backendMessage ?: ProfileRepositoryException.ERROR_SERVER)
            else -> ProfileRepositoryException.Unknown(
                backendMessage ?: "${ProfileRepositoryException.ERROR_UNKNOWN} (HTTP ${code()})"
            )
        }
    }

    private fun parseBackendError(rawMessage: String?): String? {
        if (rawMessage.isNullOrBlank()) return null
        return try {
            val jsonObject = JSONObject(rawMessage)
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
            if (jsonObject.has("title")) return jsonObject.getString("title")
            if (jsonObject.has("message")) return jsonObject.getString("message")

            rawMessage
        } catch (_: Exception) {
            rawMessage
        }
    }
}