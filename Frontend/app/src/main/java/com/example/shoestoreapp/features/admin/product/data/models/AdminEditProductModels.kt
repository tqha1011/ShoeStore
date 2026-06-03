package com.example.shoestoreapp.features.admin.product.data.models

import android.net.Uri
import com.example.shoestoreapp.features.admin.product.data.remote.ProductVariantResponseDto

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
    val variants: List<ProductVariantResponseDto>,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val bannerMessage: String = "",
    val isBannerSuccess: Boolean = true,
    val showBanner: Boolean = false
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
    val variantId: String? = null,
    val existingImageUrl: String? = null,
    val imageUri: Uri? = null,
    val imageUrl: String? = null,
    val selectedSize: ShoeSize? = null,
    val selectedColor: ShoeColor? = null,
    val price: String = "",
    val stock: String = "",
    val isUploadingImage: Boolean = false
)
