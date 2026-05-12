package com.example.shoestoreapp.features.admin.product

import android.net.Uri

data class ProductVariant(
    val size: String,
    val color: String,
    val stock: Int
)

data class Category(
    val id: Int,
    val name: String
)

data class EditProductUiState(
    val productName: String,
    val brand: String,
    val selectedCategory: Category?,
    val imageUrl: String?,
    val variants: List<ProductVariant>,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class ShoeSize(
    val id: Int,
    val value: String
)

data class ShoeColor(
    val id: Int,
    val name: String,
    val hexCode: String
)

data class VariantUiState(
    val imageUri: Uri? = null,
    val imageUrl: String? = null,
    val selectedSize: ShoeSize? = null,
    val selectedColor: ShoeColor? = null,
    val price: String = "",
    val stock: String = "",
    val isUploadingImage: Boolean = false
)
