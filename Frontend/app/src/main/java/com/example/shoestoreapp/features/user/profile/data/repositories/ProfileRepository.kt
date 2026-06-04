package com.example.shoestoreapp.features.user.profile.data.repositories

import com.example.shoestoreapp.features.user.profile.data.models.UserProfile
import com.example.shoestoreapp.features.user.profile.data.remote.ChangePasswordDto
import com.example.shoestoreapp.features.user.profile.data.remote.UpdateProfileDto

interface ProfileRepository {
    suspend fun getUserProfile(): Result<UserProfile>
    suspend fun updateProfile(dto: UpdateProfileDto): Result<Unit>
    suspend fun changePassword(dto: ChangePasswordDto): Result<Unit>
}

sealed class ProfileRepositoryException(message: String) : Exception(message) {
    class BadRequest(message: String = ERROR_BAD_REQUEST) : ProfileRepositoryException(message)
    class Unauthorized(message: String = ERROR_UNAUTHORIZED) : ProfileRepositoryException(message)
    class NotFound(message: String = ERROR_NOT_FOUND) : ProfileRepositoryException(message)
    class ServerError(message: String = ERROR_SERVER) : ProfileRepositoryException(message)
    class Unknown(message: String = ERROR_UNKNOWN) : ProfileRepositoryException(message)

    companion object {
        const val ERROR_BAD_REQUEST = "Invalid profile data. Please check your input."
        const val ERROR_UNAUTHORIZED = "Unauthorized. Please sign in again."
        const val ERROR_NOT_FOUND = "User profile not found."
        const val ERROR_SERVER = "Server error. Please try again later."
        const val ERROR_UNKNOWN = "Something went wrong with the profile service."
    }
}