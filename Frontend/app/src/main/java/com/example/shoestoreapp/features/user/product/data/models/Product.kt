package com.example.shoestoreapp.features.user.product.data.models

/**
 * Data class representing a product.
 *
 * @param id - ID sản phẩm
 * @param name - Tên sản phẩm (VD: "Nike Air Max 270")
 * @param imageUrl - URL ảnh sản phẩm
 * @param description - Mô tả sản phẩm (VD: "Red and Black Colorway")
 * @param price - Giá sản phẩm
 * @param rating - Đánh giá sao (VD: 4.5)
 * @param reviewCount - Số lượng review
 * @param category - Danh mục sản phẩm (VD: "BESTSELLER", "NEW RELEASE")
 * @param productType - Loại sản phẩm (VD: "Men's Shoes", "Running Shoes")
 * @param isFavorite - Có được yêu thích hay không
 */
data class Product(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val description: String,
    val price: Double,
    val rating: Double,
    val reviewCount: Int,
    val category: String,
    val productType: String,
    val isFavorite: Boolean = false
)

