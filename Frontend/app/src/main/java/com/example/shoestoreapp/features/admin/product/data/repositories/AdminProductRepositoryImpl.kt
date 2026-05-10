package com.example.shoestoreapp.features.admin.product.data.repositories

import com.example.shoestoreapp.core.networks.RetrofitInstance
import com.example.shoestoreapp.features.admin.product.data.models.AdminProduct
import com.example.shoestoreapp.features.admin.product.data.models.StockStatus
import com.example.shoestoreapp.features.admin.product.data.remote.AdminProductApi
import com.example.shoestoreapp.features.admin.product.data.remote.ProductResponseDto
import com.example.shoestoreapp.features.admin.product.data.remote.ProductSearchDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class AdminProductRepositoryImpl(
    private val adminApi: AdminProductApi = RetrofitInstance.adminApi
) : AdminProductRepository {
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
            emit(
                AdminProductPage(
                    items = emptyList(),
                    pageNumber = pageIndex ?: 1,
                    totalPages = pageIndex ?: 1,
                    hasNext = false
                )
            )
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
            Result.failure(AdminProductRepositoryException.Unknown(e.message ?: "Unknown error"))
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

        return AdminProduct(
            id = dto.publicId,
            name = dto.productName,
            imageUrl = dto.variants?.firstOrNull()?.imageUrl ?: "",
            price = dto.variants?.firstOrNull()?.price ?: 0.0,
            stockStatus = stockStatus,
            stock = totalStock,
            variantsCount = variantsCount
        )
    }
}
