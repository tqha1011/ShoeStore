package com.example.shoestoreapp.features.admin.product.data.remote

data class CreateProductDto(
    val productName: String,
    val categoryId: Int,
    val brand: String
)

