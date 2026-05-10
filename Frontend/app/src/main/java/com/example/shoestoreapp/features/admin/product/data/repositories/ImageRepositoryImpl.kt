package com.example.shoestoreapp.features.admin.product.data.repositories

import com.example.shoestoreapp.core.networks.RetrofitInstance
import com.example.shoestoreapp.features.admin.product.data.remote.ImageApi
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import java.io.File

private const val ERROR_BAD_REQUEST = "Invalid image file. Please try another image."
private const val ERROR_UNAUTHORIZED = "Unauthorized. Please sign in again."
private const val ERROR_SERVER = "Server error. Please try again later."
private const val ERROR_UNKNOWN = "Unable to upload image right now."

class ImageRepositoryImpl(
    private val api: ImageApi = RetrofitInstance.imageApi
) : ImageRepository {
    override suspend fun uploadImage(imageFile: File): Result<String> {
        return try {
            val requestBody = imageFile.asRequestBody("image/*".toMediaType())
            val part = MultipartBody.Part.createFormData("file", imageFile.name, requestBody)
            val response = api.uploadImage(part)

            if (response.isSuccessful) {
                val body = response.body()?.string()?.trim().orEmpty()
                if (body.isNotBlank()) {
                    Result.success(body)
                } else {
                    Result.failure(ImageRepositoryException.Unknown("Empty response body."))
                }
            } else {
                Result.failure(response.toRepositoryException())
            }
        } catch (e: Exception) {
            Result.failure(ImageRepositoryException.Unknown(e.message ?: ERROR_UNKNOWN))
        }
    }

    private fun <T> Response<T>.toRepositoryException(): ImageRepositoryException {
        val backendMessage = errorBody()?.string()?.takeIf { it.isNotBlank() }

        return when (code()) {
            400 -> ImageRepositoryException.BadRequest(backendMessage ?: ERROR_BAD_REQUEST)
            401 -> ImageRepositoryException.Unauthorized(backendMessage ?: ERROR_UNAUTHORIZED)
            500 -> ImageRepositoryException.ServerError(backendMessage ?: ERROR_SERVER)
            else -> ImageRepositoryException.Unknown(
                backendMessage ?: "$ERROR_UNKNOWN (HTTP ${code()})"
            )
        }
    }
}
