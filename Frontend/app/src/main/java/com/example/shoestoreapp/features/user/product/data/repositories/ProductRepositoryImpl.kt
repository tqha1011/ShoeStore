package com.example.shoestoreapp.features.user.product.data.repositories

import com.example.shoestoreapp.core.networks.RetrofitInstance
import com.example.shoestoreapp.features.user.product.data.models.Product
import com.example.shoestoreapp.features.user.product.data.models.ProductVariant
import com.example.shoestoreapp.features.user.product.data.remote.ProductApi
import com.example.shoestoreapp.features.user.product.data.remote.ProductSearchRequest
import com.example.shoestoreapp.features.user.product.data.remote.ProductResponseDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.json.JSONObject
import retrofit2.Response

class ProductRepositoryImpl(
    private val productApi: ProductApi = RetrofitInstance.productApi
) : ProductRepository {

    override fun searchProducts(request: ProductSearchRequest): Flow<List<Product>> = flow {
        val options = request.toMap()
        val response = productApi.searchProducts(options)

        if (response.isSuccessful) {
            val dtoList = response.body()?.items ?: emptyList()
            val productsList = dtoList.map { dto -> mapDtoToProduct(dto) }
            emit(productsList)
        } else {
            throw response.toRepositoryException()
        }
    }.flowOn(Dispatchers.IO)

    override fun getProductDetail(productGuid: String): Flow<Product> = flow {
        val response = productApi.getProductDetail(productGuid)

        if (response.isSuccessful) {
            val body = response.body()?.data
            if (body != null) {
                emit(mapDtoToProduct(body))
            } else {
                throw ProductRepositoryException.NotFound(ProductRepositoryException.ERROR_NOT_FOUND)
            }
        } else {
            throw response.toRepositoryException()
        }
    }.flowOn(Dispatchers.IO)

    private fun mapDtoToProduct(dto: ProductResponseDto): Product {
        val variants = dto.variants.map { variantDto ->
            ProductVariant(
                publicId = variantDto.publicId,
                sizeId = variantDto.sizeId,
                size = variantDto.size,
                colorId = variantDto.colorId,
                colorName = variantDto.colorName,
                stock = variantDto.stock,
                price = variantDto.price,
                imageUrl = variantDto.imageUrl,
                isSelling = variantDto.isSelling,
                isDelete = variantDto.isDelete
            )
        }
        return Product(
            publicId = dto.publicId,
            productName = dto.productName,
            categoryName = dto.categoryName,
            variants = variants
        )
    }

    // =======================================================
    // LOGIC PARSE LỖI ĐỘC QUYỀN
    // =======================================================
    private fun <T> Response<T>.toRepositoryException(): ProductRepositoryException {
        val rawMessage = errorBody()?.string()?.takeIf { it.isNotBlank() }
        val backendMessage = parseBackendError(rawMessage)

        return when (code()) {
            400 -> ProductRepositoryException.BadRequest(backendMessage ?: ProductRepositoryException.ERROR_BAD_REQUEST)
            401 -> ProductRepositoryException.Unauthorized(backendMessage ?: ProductRepositoryException.ERROR_UNAUTHORIZED)
            404 -> ProductRepositoryException.NotFound(backendMessage ?: ProductRepositoryException.ERROR_NOT_FOUND)
            500 -> ProductRepositoryException.ServerError(backendMessage ?: ProductRepositoryException.ERROR_SERVER)
            else -> ProductRepositoryException.Unknown(
                backendMessage ?: "${ProductRepositoryException.ERROR_UNKNOWN} (HTTP ${code()})"
            )
        }
    }

    private fun parseBackendError(rawMessage: String?): String? {
        if (rawMessage.isNullOrBlank()) return null
        return try {
            val jsonObject = JSONObject(rawMessage)

            if (jsonObject.has("errors")) {
                val errorsObj = jsonObject.getJSONObject("errors")
                val errorMessages = mutableListOf<String>()
                val keys = errorsObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val errorArray = errorsObj.getJSONArray(key)
                    for (i in 0 until errorArray.length()) {
                        errorMessages.add(errorArray.getString(i))
                    }
                }
                if (errorMessages.isNotEmpty()) return errorMessages.joinToString("\n")
            }
            if (jsonObject.has("detail")) return jsonObject.getString("detail")
            if (jsonObject.has("message")) return jsonObject.getString("message")
            if (jsonObject.has("title")) return jsonObject.getString("title")

            rawMessage
        } catch (_: Exception) {
            rawMessage
        }
    }
}