package com.example.shoestoreapp.features.user.product.data.models

/**
 * Data class representing a product.
 * Match với Backend ProductResponseDto
 *
 * @param publicId - GUID của sản phẩm (từ Backend)
 * @param productName - Tên sản phẩm (VD: "Nike Air Max 270")
 * @param brand - Thương hiệu (VD: "Nike")
 * @param variants - Danh sách các biến thể (size, color, price, stock)
 */
data class Product(
    val publicId: String,
    val productName: String,
    val brand: String,
    val variants: List<ProductVariant> = emptyList()
)

/**
 * ProductVariant: Biến thể của một sản phẩm
 * Match với Backend ProductVariantResponseDto
 * 
 * @param publicId - GUID của variant
 * @param sizeId - ID kích cỡ
 * @param size - Giá trị kích cỡ (VD: 10 cho size 10 shoes)
 * @param colorId - ID màu sắc
 * @param colorName - Tên màu (VD: "Black")
 * @param stock - Số lượng tồn kho
 * @param price - Giá bán
 * @param imageUrl - URL ảnh variant
 * @param isSelling - Có đang bán không
 * @param isDelete - Đã xóa không
 */
data class ProductVariant(
    val publicId: String,
    val sizeId: Int,
    val size: Int? = null,
    val colorId: Int? = null,
    val colorName: String? = null,
    val stock: Int,
    val price: Double,
    val imageUrl: String? = null,
    val isSelling: Boolean = true,
    val isDelete: Boolean = false
)



