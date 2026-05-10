package com.example.shoestoreapp.features.admin.product

data class ProductVariant(
    val size: String,
    val color: String,
    val stock: Int
)

data class EditProductUiState(
    val productName: String,
    val brand: String,
    val category: String,
    val imageUrl: String?,
    val variants: List<ProductVariant>,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
