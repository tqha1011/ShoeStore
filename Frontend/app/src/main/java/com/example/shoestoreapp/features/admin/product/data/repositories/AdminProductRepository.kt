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
    // 1. Search products with filters and pagination
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
    }.catch { _ ->
        emit(emptyList())

    }.flowOn(Dispatchers.IO)

    // 2. Get product detail by productGuid
    fun adminGetProductDetail(productGuid: String): Flow<AdminProduct?> = flow {
        val response = adminApi.adminSearchDetail(productGuid)
        if (response.isSuccessful && response.body() != null) {
            val body = response.body()
            val product = body?.let { mapDtoToAdminProduct(it) }
            emit(product)
        } else {
            emit(null)
        }
    }.catch { _ ->
        emit(null)
    }.flowOn(Dispatchers.IO)

    private fun mapDtoToAdminProduct(dto: ProductSearchDto): AdminProduct {
        return AdminProduct(
            id = dto.publicId,
            name = dto.productName,
            imageUrl = dto.variants?.firstOrNull()?.imageUrl ?: "",
            price = dto.variants?.firstOrNull()?.price ?: 0.0,
            stockStatus = when (dto.variants?.firstOrNull()?.stockStatus) {
                "In Stock" -> StockStatus.IN_STOCK
                "Low Stock" -> StockStatus.LOW_STOCK
                "Out Of Stock" -> StockStatus.OUT_OF_STOCK
                else -> StockStatus.IN_STOCK
            },
            stock = dto.variants?.firstOrNull()?.stock ?: 0
        )
    }
}