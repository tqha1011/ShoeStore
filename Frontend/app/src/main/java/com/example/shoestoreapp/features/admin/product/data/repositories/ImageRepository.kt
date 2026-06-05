package com.example.shoestoreapp.features.admin.product.data.repositories

import com.example.shoestoreapp.features.admin.product.data.remote.ImageUploadResponseDto
import java.io.File

fun interface ImageRepository {
    suspend fun uploadImage(imageFile: File): Result<ImageUploadResponseDto>
}

sealed class ImageRepositoryException(message: String) : Exception(message) {
    class BadRequest(message: String = ERROR_BAD_REQUEST) : ImageRepositoryException(message)
    class Unauthorized(message: String = ERROR_UNAUTHORIZED) : ImageRepositoryException(message)
    class ServerError(message: String = ERROR_SERVER) : ImageRepositoryException(message)
    class Unknown(message: String = ERROR_UNKNOWN) : ImageRepositoryException(message)

    companion object {
        const val ERROR_BAD_REQUEST = "Invalid image file. Please try another image."
        const val ERROR_UNAUTHORIZED = "Unauthorized. Please sign in again."
        const val ERROR_SERVER = "Server error. Please try again later."
        const val ERROR_UNKNOWN = "Unable to upload image right now."
    }
}