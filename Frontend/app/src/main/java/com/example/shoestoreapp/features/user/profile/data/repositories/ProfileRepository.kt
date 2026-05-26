package com.example.shoestoreapp.features.user.profile.data.repositories

import com.example.shoestoreapp.features.user.profile.data.models.UserProfile
import com.example.shoestoreapp.features.user.profile.data.remote.UpdateProfileDto
import com.example.shoestoreapp.features.user.profile.data.remote.ChangePasswordDto

interface ProfileRepository {
    suspend fun getUserProfile(): Result<UserProfile>
    suspend fun updateProfile(dto: UpdateProfileDto): Result<Unit>
    suspend fun changePassword(dto: ChangePasswordDto): Result<Unit>
}
