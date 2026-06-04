package com.example.shoestoreapp.features.admin.addproduct.data.repositories

import com.example.shoestoreapp.core.networks.RetrofitInstance
import com.example.shoestoreapp.features.admin.product.data.remote.AdminProductApi
import com.example.shoestoreapp.features.admin.product.data.remote.CreateProductDto
import com.example.shoestoreapp.core.utils.ApiErrorHandler
import retrofit2.Response

fun interface AdminProductRepository {
    suspend fun createProduct(dto: CreateProductDto): Result<Unit>
}

sealed class AdminProductRepositoryException(message: String) : Exception(message) {
    class BadRequest(message: String = ERROR_BAD_REQUEST) : AdminProductRepositoryException(message)
    class Unauthorized(message: String = ERROR_UNAUTHORIZED) : AdminProductRepositoryException(message)
    class ServerError(message: String = ERROR_SERVER) : AdminProductRepositoryException(message)
    class Unknown(message: String = ERROR_UNKNOWN) : AdminProductRepositoryException(message)
}

private const val ERROR_BAD_REQUEST = "Invalid product data. Please check your input."
private const val ERROR_UNAUTHORIZED = "Unauthorized. Please sign in again."
private const val ERROR_SERVER = "Server error. Please try again later."
private const val ERROR_UNKNOWN = "Unable to create product right now."

class AdminProductRepositoryImpl(
    private val api: AdminProductApi = RetrofitInstance.adminApi
) : AdminProductRepository {
    override suspend fun createProduct(dto: CreateProductDto): Result<Unit> {
        return try {
            val response = api.createProduct(dto)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(response.toRepositoryException())
            }
        } catch (e: Exception) {
            Result.failure(AdminProductRepositoryException.Unknown(e.message ?: ERROR_UNKNOWN))
        }
    }

    private fun <T> Response<T>.toRepositoryException(): AdminProductRepositoryException {
        val backendMessage = ApiErrorHandler.extractErrorMessage(this)

        return when (code()) {
            400 -> AdminProductRepositoryException.BadRequest(backendMessage ?: ERROR_BAD_REQUEST)
            401 -> AdminProductRepositoryException.Unauthorized(backendMessage ?: ERROR_UNAUTHORIZED)
            500 -> AdminProductRepositoryException.ServerError(backendMessage ?: ERROR_SERVER)
            else -> AdminProductRepositoryException.Unknown(
                backendMessage ?: "$ERROR_UNKNOWN (HTTP ${code()})"
            )
        }
    }
}