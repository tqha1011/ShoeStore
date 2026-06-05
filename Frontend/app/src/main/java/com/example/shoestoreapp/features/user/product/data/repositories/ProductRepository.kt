package com.example.shoestoreapp.features.user.product.data.repositories

import com.example.shoestoreapp.features.user.product.data.models.Product
import com.example.shoestoreapp.features.user.product.data.remote.ProductSearchRequest
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun searchProducts(request: ProductSearchRequest): Flow<List<Product>>
    fun getProductDetail(productGuid: String): Flow<Product>
}

sealed class ProductRepositoryException(message: String) : Exception(message) {
    class BadRequest(message: String = ERROR_BAD_REQUEST) : ProductRepositoryException(message)
    class Unauthorized(message: String = ERROR_UNAUTHORIZED) : ProductRepositoryException(message)
    class NotFound(message: String = ERROR_NOT_FOUND) : ProductRepositoryException(message)
    class ServerError(message: String = ERROR_SERVER) : ProductRepositoryException(message)
    class Unknown(message: String = ERROR_UNKNOWN) : ProductRepositoryException(message)

    companion object {
        const val ERROR_BAD_REQUEST = "Invalid product request."
        const val ERROR_UNAUTHORIZED = "Unauthorized access."
        const val ERROR_NOT_FOUND = "Product not found."
        const val ERROR_SERVER = "Server error. Please try again later."
        const val ERROR_UNKNOWN = "Something went wrong while fetching products."
    }
}