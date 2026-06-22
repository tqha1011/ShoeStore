package com.example.shoestoreapp.features.admin.product.data.repositories

import com.example.shoestoreapp.core.networks.RetrofitInstance
import com.example.shoestoreapp.features.admin.product.data.models.AdminProduct
import com.example.shoestoreapp.features.admin.product.data.models.StockStatus
import com.example.shoestoreapp.features.admin.product.data.remote.AdminProductApi
import com.example.shoestoreapp.features.admin.product.data.remote.ProductResponseDto
import com.example.shoestoreapp.features.admin.product.data.remote.ProductSearchDto
import com.example.shoestoreapp.features.admin.product.data.remote.ProductVariantResponseDto
import com.example.shoestoreapp.features.admin.product.data.remote.UpdateProductDto
import com.example.shoestoreapp.core.utils.ApiErrorHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response

class AdminProductRepositoryImpl(
    private val adminApi: AdminProductApi = RetrofitInstance.adminApi
) : AdminProductRepository {

    companion object {
        private const val UNKNOWN_ERROR_MESSAGE = "Unknown error"
    }

    override fun searchProducts(
        keyword: String?,
        inStock: Boolean?,
        outOfStock: Boolean?,
        lowStock: Boolean?,
        pageIndex: Int?,
        pageSize: Int?
    ): Flow<AdminProductPage> = flow {
        val response = adminApi.adminSearchProducts(
            keyword,
            inStock,
            outOfStock,
            lowStock,
            pageIndex,
            pageSize
        )

        if (response.isSuccessful && response.body() != null) {
            val body = response.body()

            val dtoList = body?.items ?: emptyList()
            val productsList = dtoList.map { dto -> mapDtoToAdminProduct(dto) }

            emit(
                AdminProductPage(
                    items = productsList,
                    pageNumber = body?.pageNumber ?: (pageIndex ?: 1),
                    totalPages = body?.totalPages ?: 1,
                    hasNext = body?.hasNext ?: false
                )
            )
        } else {
            // Ném lỗi lên Flow để ViewModel xử lý thay vì im lặng trả về list rỗng
            throw response.toRepositoryException()
        }
    }.catch { _ ->
        emit(
            AdminProductPage(
                items = emptyList(),
                pageNumber = pageIndex ?: 1,
                totalPages = pageIndex ?: 1,
                hasNext = false
            )
        )
    }.flowOn(Dispatchers.IO)

    override suspend fun getProductById(productId: String): Result<ProductResponseDto> {
        return try {
            val response = adminApi.getProductById(productId)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(response.toRepositoryException())
            }
        } catch (e: Exception) {
            Result.failure(AdminProductRepositoryException.Unknown(e.message ?: UNKNOWN_ERROR_MESSAGE))
        }
    }

    override suspend fun updateProduct(
        productId: String,
        data: UpdateProductDto
    ): Result<ProductResponseDto> {
        return try {
            val response = adminApi.updateProduct(productId, data)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(response.toRepositoryException())
            }
        } catch (e: Exception) {
            Result.failure(AdminProductRepositoryException.Unknown(e.message ?: UNKNOWN_ERROR_MESSAGE))
        }
    }

    override suspend fun deleteProduct(productId: String): Result<Unit> {
        return try {
            val response = adminApi.deleteProduct(productId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(response.toRepositoryException())
            }
        } catch (e: Exception) {
            Result.failure(AdminProductRepositoryException.Unknown(e.message ?: UNKNOWN_ERROR_MESSAGE))
        }
    }

    override suspend fun createVariant(
        productId: String,
        params: CreateVariantParams
    ): Result<ProductVariantResponseDto> {
        val textMediaType = "text/plain".toMediaType()
        val sizeBody: RequestBody = params.sizeId.toString().toRequestBody(textMediaType)
        val colorBody: RequestBody = params.colorId.toString().toRequestBody(textMediaType)
        val stockBody: RequestBody = params.stock.toString().toRequestBody(textMediaType)
        val priceBody: RequestBody = params.price.toString().toRequestBody(textMediaType)
        val sellingBody: RequestBody = params.isSelling.toString().toRequestBody(textMediaType)
        val imageUrlBody: RequestBody = "".toRequestBody(textMediaType)

        val imagePart = params.imageFile?.let { file ->
            val imageRequest = file.asRequestBody("image/*".toMediaType())
            MultipartBody.Part.createFormData("image", file.name, imageRequest)
        }

        return try {
            val response = adminApi.createVariant(
                productId = productId,
                sizeId = sizeBody,
                colorId = colorBody,
                stock = stockBody,
                price = priceBody,
                isSelling = sellingBody,
                imageUrl = imageUrlBody,
                image = imagePart
            )
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(response.toRepositoryException())
            }
        } catch (e: Exception) {
            Result.failure(AdminProductRepositoryException.Unknown(e.message ?: UNKNOWN_ERROR_MESSAGE))
        }
    }

    override suspend fun updateVariant(
        productId: String,
        variantId: String,
        params: UpdateVariantParams
    ): Result<Unit> {
        val textMediaType = "text/plain".toMediaType()
        val sizeBody: RequestBody = params.sizeId.toString().toRequestBody(textMediaType)
        val colorBody: RequestBody = params.colorId.toString().toRequestBody(textMediaType)
        val stockBody: RequestBody = params.stock.toString().toRequestBody(textMediaType)
        val priceBody: RequestBody = params.price.toString().toRequestBody(textMediaType)
        val sellingBody: RequestBody = params.isSelling.toString().toRequestBody(textMediaType)
        val imageUrlBody: RequestBody = params.imageUrl.toRequestBody(textMediaType)

        val imagePart = params.imageFile?.let { file ->
            val imageRequest = file.asRequestBody("image/*".toMediaType())
            MultipartBody.Part.createFormData("image", file.name, imageRequest)
        }

        return try {
            val response = adminApi.updateVariant(
                productId = productId,
                variantId = variantId,
                sizeId = sizeBody,
                colorId = colorBody,
                stock = stockBody,
                price = priceBody,
                isSelling = sellingBody,
                imageUrl = imageUrlBody,
                image = imagePart
            )
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(response.toRepositoryException())
            }
        } catch (e: Exception) {
            Result.failure(AdminProductRepositoryException.Unknown(e.message ?: UNKNOWN_ERROR_MESSAGE))
        }
    }

    override suspend fun deleteVariant(productId: String, variantId: String): Result<Unit> {
        return try {
            val response = adminApi.deleteVariant(productId, variantId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(response.toRepositoryException())
            }
        } catch (e: Exception) {
            Result.failure(AdminProductRepositoryException.Unknown(e.message ?: UNKNOWN_ERROR_MESSAGE))
        }
    }

    private fun mapDtoToAdminProduct(dto: ProductSearchDto): AdminProduct {
        val totalStock = dto.variants?.sumOf { it.stock ?: 0 } ?: 0
        val stockStatus = when {
            totalStock == 0 -> StockStatus.OUT_OF_STOCK
            totalStock < 10 -> StockStatus.LOW_STOCK
            else -> StockStatus.IN_STOCK
        }
        val variantsCount = dto.variants?.size ?: 0
        val representativeImageUrl = dto.variants
            ?.firstOrNull { !it.imageUrl.isNullOrBlank() }
            ?.imageUrl

        return AdminProduct(
            id = dto.publicId,
            name = dto.productName,
            imageUrl = representativeImageUrl,
            price = dto.variants?.firstOrNull()?.price ?: 0.0,
            stockStatus = stockStatus,
            stock = totalStock,
            variantsCount = variantsCount
        )
    }

    internal fun <T> Response<T>.toRepositoryException(): AdminProductRepositoryException {
        val backendError = ApiErrorHandler.extractErrorDetails(this)
        val friendlyMessage = backendError.code.toFriendlyAdminProductMessage()
        val backendMessage = friendlyMessage ?: backendError.message

        return when (code()) {
            400 -> AdminProductRepositoryException.BadRequest(backendMessage ?: ERROR_BAD_REQUEST)
            401 -> AdminProductRepositoryException.Unauthorized(backendMessage ?: ERROR_UNAUTHORIZED)
            404 -> AdminProductRepositoryException.NotFound(backendMessage ?: ERROR_NOT_FOUND)
            409 -> AdminProductRepositoryException.Conflict(backendMessage ?: ERROR_CONFLICT)
            500 -> AdminProductRepositoryException.ServerError(backendMessage ?: ERROR_SERVER)
            else -> AdminProductRepositoryException.Unknown(
                backendMessage ?: "$ERROR_UNKNOWN (HTTP ${code()})"
            )
        }
    }

    private fun String?.toFriendlyAdminProductMessage(): String? {
        return when (this) {
            "Product.NotFound" ->
                "The selected product could not be found. Refresh the product list and try again."
            "ProductVariant.InvalidInput" ->
                "Please check the variant size, color, price, and stock before trying again."
            "ProductVariant.Exists" ->
                "A variant with this size and color already exists for this product."
            else -> null
        }
    }
}
