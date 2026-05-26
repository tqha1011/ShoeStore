package com.example.shoestoreapp.features.user.profile.data.repositories

import com.example.shoestoreapp.features.user.profile.data.models.UserProfile

interface ProfileRepository {
    suspend fun getUserProfile(): Result<UserProfile>
}

