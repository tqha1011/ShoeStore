package com.example.shoestoreapp.features.user.product.data.repositories

import com.example.shoestoreapp.core.networks.RetrofitInstance
import com.example.shoestoreapp.features.user.product.data.models.Product
import com.example.shoestoreapp.features.user.product.data.models.ProductVariant
import com.example.shoestoreapp.features.user.product.data.remote.ProductApi
import com.example.shoestoreapp.features.user.product.data.remote.ProductSearchRequest
import com.example.shoestoreapp.features.user.product.data.remote.ProductResponseDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn


class ProductRepository(
    private val productApi: ProductApi = RetrofitInstance.productApi
) {
    fun searchProducts(request: ProductSearchRequest): Flow<List<Product>?> = flow {
        val options = request.toMap()
        val response = productApi.searchProducts(options)

        if (response.isSuccessful && response.body() != null) {
            val body = response.body()

            val dtoList = body?.items ?: emptyList()
            val productsList = dtoList.map { dto -> mapDtoToProduct(dto)}

            emit(productsList)
        } else {
            emit(emptyList())
        }
    }.catch {
        _ -> emit(emptyList())

    }.flowOn(Dispatchers.IO)


    fun getProductDetail(productGuid: String): Flow<Product?> = flow {
        val response = productApi.getProductDetail(productGuid)
        if (response.isSuccessful && response.body() != null) {
            val body = response.body()?.data
            val product = body?.let {mapDtoToProduct(it)}
            emit(product)
        } else {
            emit(null)
        }
    }.catch {
        _ -> emit(null)
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
            brand = dto.brand,
            variants = variants
        )
    }
}

