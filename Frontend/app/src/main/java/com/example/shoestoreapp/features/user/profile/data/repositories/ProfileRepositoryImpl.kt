package com.example.shoestoreapp.features.user.profile.data.repositories

import com.example.shoestoreapp.core.networks.RetrofitInstance
import com.example.shoestoreapp.features.user.profile.data.remote.ProfileApi
import com.example.shoestoreapp.features.user.profile.data.remote.toUserProfile
import com.example.shoestoreapp.features.user.profile.data.models.UserProfile

class ProfileRepositoryImpl(
    private val api: ProfileApi = RetrofitInstance.profileApi
) : ProfileRepository {
    override suspend fun getUserProfile(): Result<UserProfile> {
        return try {
            val response = api.getUserProfile()
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it.toUserProfile()) }
                    ?: Result.failure(Exception("Empty response body."))
            } else {
                Result.failure(Exception("Fetch profile failed (HTTP ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
