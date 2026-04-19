package com.example.shoestoreapp.features.admin.crud.data.repositories

import com.example.shoestoreapp.core.networks.RetrofitInstance
import com.example.shoestoreapp.features.admin.crud.data.remote.AdminProductCrudApi
import com.example.shoestoreapp.features.admin.crud.data.remote.ProductUpdateDtoRequest
import com.example.shoestoreapp.core.utils.Resource
import com.example.shoestoreapp.features.admin.crud.data.remote.ProductCreateDtoRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.json.JSONObject
import retrofit2.Response

class ProductCrudRepository(
    private val productCrudApi: AdminProductCrudApi = RetrofitInstance.adminCrudApi
) {
    /**
     * Map HTTP status codes to descriptive error messages in English
     */
    private fun getErrorMessage(statusCode: Int, defaultMessage: String): String {
        return when (statusCode) {
            in 400..499 -> {
                when (statusCode) {
                    400 -> "Invalid input data"
                    401 -> "Unauthorized (Please login)"
                    403 -> "Access denied"
                    404 -> "Product not found"
                    409 -> "Data conflict or already exists"
                    422 -> "Invalid data"
                    else -> "Client error (Code: $statusCode)"
                }
            }
            in 500..599 -> {
                when (statusCode) {
                    500 -> "Internal server error"
                    502 -> "Bad gateway"
                    503 -> "Service unavailable"
                    else -> "Server error (Code: $statusCode)"
                }
            }
            else -> defaultMessage
        }
    }


    private fun <T> extractErrorMessage(response: Response<T>, defaultMessage: String): String {
        val errorString = response.errorBody()?.string()

        if (!errorString.isNullOrEmpty()) {
            try {
                val jsonObject = JSONObject(errorString)
                if (jsonObject.has("errors")) {
                    val errorsObj = jsonObject.getJSONObject("errors")
                    val keys = errorsObj.keys()

                    if (keys.hasNext()) {
                        val firstKey = keys.next()
                        val errorArray = errorsObj.getJSONArray(firstKey)
                        if (errorArray.length() > 0) {
                            return errorArray.getString(0)
                        }
                    }
                }

                if (jsonObject.has("title")) {
                    return jsonObject.getString("title")
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return getErrorMessage(statusCode = response.code(), defaultMessage)
    }
    // 1. Create product
    fun adminCreateProduct(
        request: ProductCreateDtoRequest
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        val response = productCrudApi.adminCreateProduct(request)

        if (response.isSuccessful) {
            emit(Resource.Success(Unit))
        } else {
            val errorMsg = extractErrorMessage(response, "Failed to create product")
            emit(Resource.Error(errorMsg))
        }
    }.catch { _ ->
        emit(Resource.Error("Connection error: Please check your network connection"))
    }.flowOn(Dispatchers.IO)

    // 2. Update product
    fun adminUpdateProduct(
        productGuid: String,
        request: ProductUpdateDtoRequest
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        val response = productCrudApi.adminUpdateProduct(productGuid, request)

        if (response.isSuccessful) {
            emit(Resource.Success(Unit))
        } else {
            val errorMsg = extractErrorMessage(response, "Failed to update product")
            emit(Resource.Error(errorMsg))
        }
    }.catch { _ ->
        emit(Resource.Error("Connection error: Please check your network connection"))
    }.flowOn(Dispatchers.IO)

    // 3. Delete product by productGuid
    fun adminDeleteProduct(productGuid: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        val response = productCrudApi.adminDeleteProduct(productGuid)

        if (response.isSuccessful) {
            emit(Resource.Success(Unit))
        } else {
            val errorMsg = extractErrorMessage(response, "Failed to delete product")
            emit(Resource.Error(errorMsg))
        }
    }.catch { _ ->
        emit(Resource.Error("Connection error: Please check your network connection"))
    }.flowOn(Dispatchers.IO)
}
