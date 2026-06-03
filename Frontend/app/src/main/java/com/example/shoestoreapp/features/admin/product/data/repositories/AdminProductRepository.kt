package com.example.shoestoreapp.features.admin.product.data.repositories

import com.example.shoestoreapp.features.admin.product.data.models.AdminProduct
import com.example.shoestoreapp.features.admin.product.data.remote.ProductResponseDto
import com.example.shoestoreapp.features.admin.product.data.remote.ProductVariantResponseDto
import com.example.shoestoreapp.features.admin.product.data.remote.UpdateProductDto
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import java.io.File

data class AdminProductPage(
    val items: List<AdminProduct>,
    val pageNumber: Int,
    val totalPages: Int,
    val hasNext: Boolean
)

data class CreateVariantParams(
    val sizeId: Int,
    val colorId: Int,
    val stock: Int,
    val price: Double,
    val isSelling: Boolean,
    val imageFile: File?
)

data class UpdateVariantParams(
    val sizeId: Int,
    val colorId: Int,
    val stock: Int,
    val price: Double,
    val isSelling: Boolean,
    val imageUrl: String,
    val imageFile: File?
)

interface AdminProductRepository {
    fun searchProducts(
        keyword: String?,
        inStock: Boolean?,
        outOfStock: Boolean?,
        lowStock: Boolean?,
        pageIndex: Int?,
        pageSize: Int?
    ): Flow<AdminProductPage>

    suspend fun getProductById(productId: String): Result<ProductResponseDto>

    suspend fun updateProduct(productId: String, data: UpdateProductDto): Result<ProductResponseDto>

    suspend fun deleteProduct(productId: String): Result<Unit>

    suspend fun createVariant(
        productId: String,
        params: CreateVariantParams
    ): Result<ProductVariantResponseDto>

    suspend fun updateVariant(
        productId: String,
        variantId: String,
        params: UpdateVariantParams
    ): Result<Unit>

    suspend fun deleteVariant(productId: String, variantId: String): Result<Unit>
}

sealed class AdminProductRepositoryException(message: String) : Exception(message) {
    class BadRequest(message: String = ERROR_BAD_REQUEST) : AdminProductRepositoryException(message)
    class Unauthorized(message: String = ERROR_UNAUTHORIZED) : AdminProductRepositoryException(message)
    class NotFound(message: String = ERROR_NOT_FOUND) : AdminProductRepositoryException(message)
    class ServerError(message: String = ERROR_SERVER) : AdminProductRepositoryException(message)
    class Unknown(message: String = ERROR_UNKNOWN) : AdminProductRepositoryException(message)
}

private const val ERROR_BAD_REQUEST = "Invalid product data. Please check your input."
private const val ERROR_UNAUTHORIZED = "Unauthorized. Please sign in again."
private const val ERROR_NOT_FOUND = "Product not found."
private const val ERROR_SERVER = "Server error. Please try again later."
private const val ERROR_UNKNOWN = "Unable to load product details right now."

internal fun <T> Response<T>.toRepositoryException(): AdminProductRepositoryException {
    val backendMessage = errorBody()?.string()?.takeIf { it.isNotBlank() }

    return when (code()) {
        400 -> AdminProductRepositoryException.BadRequest(backendMessage ?: ERROR_BAD_REQUEST)
        401 -> AdminProductRepositoryException.Unauthorized(backendMessage ?: ERROR_UNAUTHORIZED)
        404 -> AdminProductRepositoryException.NotFound(backendMessage ?: ERROR_NOT_FOUND)
        500 -> AdminProductRepositoryException.ServerError(backendMessage ?: ERROR_SERVER)
        else -> AdminProductRepositoryException.Unknown(
            backendMessage ?: "$ERROR_UNKNOWN (HTTP ${code()})"
        )
    }
}