package com.example.shoestoreapp.features.admin.product.data.repositories

import com.example.shoestoreapp.core.networks.RetrofitInstance
import com.example.shoestoreapp.core.utils.ApiErrorHandler
import com.example.shoestoreapp.features.admin.product.data.remote.ImageApi
import com.example.shoestoreapp.features.admin.product.data.remote.ImageUploadResponseDto
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import java.io.File

class ImageRepositoryImpl(
    private val api: ImageApi = RetrofitInstance.imageApi
) : ImageRepository {
    override suspend fun uploadImage(imageFile: File): Result<ImageUploadResponseDto> {
        return try {
            val requestBody = imageFile.asRequestBody("image/*".toMediaType())
            val part = MultipartBody.Part.createFormData("file", imageFile.name, requestBody)
            val response = api.uploadImage(part)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.imageUrl.isNotBlank()) {
                    Result.success(body)
                } else {
                    Result.failure(ImageRepositoryException.Unknown("Empty response body."))
                }
            } else {
                Result.failure(response.toRepositoryException())
            }
        } catch (e: Exception) {
            Result.failure(ImageRepositoryException.Unknown(e.message ?: ImageRepositoryException.ERROR_UNKNOWN))
        }
    }

    private fun <T> Response<T>.toRepositoryException(): ImageRepositoryException {
        val backendMessage = ApiErrorHandler.extractErrorMessage(this)
        return when (code()) {
            400 -> ImageRepositoryException.BadRequest(backendMessage ?: ImageRepositoryException.ERROR_BAD_REQUEST)
            401 -> ImageRepositoryException.Unauthorized(backendMessage ?: ImageRepositoryException.ERROR_UNAUTHORIZED)
            500 -> ImageRepositoryException.ServerError(backendMessage ?: ImageRepositoryException.ERROR_SERVER)
            else -> ImageRepositoryException.Unknown(
                backendMessage ?: "${ImageRepositoryException.ERROR_UNKNOWN} (HTTP ${code()})"
            )
        }
    }

}