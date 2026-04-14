package com.example.shoestoreapp.features.admin.crud.data.repositories

import com.example.shoestoreapp.core.networks.RetrofitInstance
import com.example.shoestoreapp.features.admin.crud.data.remote.AdminProductCrudApi
import com.example.shoestoreapp.features.admin.crud.data.remote.ProductVariantDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Response

class ProductCrudRepository(
    private val productCrudApi: AdminProductCrudApi = RetrofitInstance.adminCrudApi
) {

    // 1. Delete product by productGuid
    fun adminDeleteProduct(productGuid: String): Flow<Boolean> = flow {
        val response = productCrudApi.adminDeleteProduct(productGuid)
        emit(response.isSuccessful)
    }.catch { e ->
        emit(false)
    }.flowOn(Dispatchers.IO)

    // 2. Create product
    fun adminCreateProduct(
        productName: String?,
        variants: List<ProductVariantDto?>,
        brand: String?
    ): Flow<Boolean> = flow {
        val response = productCrudApi.adminCreateProduct(productName, variants, brand)
        emit(response.isSuccessful)
    }.catch { e ->
        emit(false)
    }.flowOn(Dispatchers.IO)

    // 3. Update product
    fun adminUpdateProduct(
        productGuid: String,
        productName: String?,
        variants: List<ProductVariantDto?>
    ): Flow<Boolean> = flow {
        val response = productCrudApi.adminUpdateProduct(productGuid, productName, variants)
        emit(response.isSuccessful)
    }.catch { e ->
        emit(false)
    }.flowOn(Dispatchers.IO)
}
