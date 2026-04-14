package com.example.shoestoreapp.features.admin.product.data.repositories
import com.example.shoestoreapp.core.networks.RetrofitInstance
import com.example.shoestoreapp.features.admin.product.data.models.AdminProduct
import com.example.shoestoreapp.features.admin.product.data.models.StockStatus
import com.example.shoestoreapp.features.admin.product.data.remote.AdminProductApi
import com.example.shoestoreapp.features.admin.product.data.remote.ProductSearchDto
import com.example.shoestoreapp.features.admin.product.data.remote.ProductVariantDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class AdminProductRepository(
    private val adminApi: AdminProductApi = RetrofitInstance.adminApi
) {
    fun searchProducts(
        keyword: String?,
        inStock: Boolean?,
        outOfStock: Boolean?,
        lowStock: Boolean?,
        pageIndex: Int?,
        pageSize: Int?
    ): Flow<List<AdminProduct>?> = flow {
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

            emit(productsList)
        } else {
            emit(emptyList())
        }
    }.catch { e ->
        emit(emptyList())

    }.flowOn(Dispatchers.IO)

    fun adminGetProductDetail(productGuid: String): Flow<AdminProduct?> = flow {
        val response = adminApi.adminSearchDetail(productGuid)
        if (response.isSuccessful && response.body() != null) {
            val body = response.body()
            val product = body?.let { mapDtoToAdminProduct(it) }
            emit(product)
        } else {
            emit(null)
        }
    }.catch { e ->
        emit(null)
    }.flowOn(Dispatchers.IO)

    fun adminDeleteProduct(productGuid: String): Flow<Boolean> = flow {
        val response = adminApi.adminDeleteProduct(productGuid)
        emit(response.isSuccessful)
    }.catch { e ->
        emit(false)
    }.flowOn(Dispatchers.IO)

    fun adminCreateProduct(
        productName: String?,
        variants: List<ProductVariantDto?>
    ): Flow<Boolean> = flow {
        val response = adminApi.adminCreateProduct(productName, variants)
        emit(response.isSuccessful)
    }.catch { e ->
        emit(false)
    }.flowOn(Dispatchers.IO)

    fun adminUpdateProduct(
        productGuid: String,
        productName: String?,
        variants: List<ProductVariantDto?>
    ): Flow<Boolean> = flow {
        val response = adminApi.adminUpdateProduct(productGuid, productName, variants)
        emit(response.isSuccessful)
    }.catch { e ->
        emit(false)
    }.flowOn(Dispatchers.IO)

    private fun mapDtoToAdminProduct(dto: ProductSearchDto): AdminProduct {
        return AdminProduct(
            id = dto.publicId,
            name = dto.productName,
            imageUrl = dto.variants?.firstOrNull()?.imageUrl ?: "",
            price = dto.variants?.firstOrNull()?.price ?: 0.0,
            stockStatus = when (dto.variants?.firstOrNull()?.stockStatus) {
                "InStock" -> StockStatus.IN_STOCK
                "LowStock" -> StockStatus.LOW_STOCK
                "OutOfStock" -> StockStatus.OUT_OF_STOCK
                else -> StockStatus.IN_STOCK
            },
            stock = dto.variants?.firstOrNull()?.stock ?: 0
        )
    }
}