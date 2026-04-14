package com.example.shoestoreapp.features.admin.product.data.models

/**
 * Enum class để xác định trạng thái tồn kho của sản phẩm.
 * 
 * @param IN_STOCK - Sản phẩm còn hàng
 * @param LOW_STOCK - Sản phẩm sắp hết hàng (dưới 10 cái)
 * @param OUT_OF_STOCK - Sản phẩm đã hết hàng
 */
enum class StockStatus {
    IN_STOCK,
    LOW_STOCK,
    OUT_OF_STOCK
}

data class AdminProduct(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val description: String,
    val price: Double,
    val stockStatus: StockStatus,
    val quantity: Int,
    val category: String,
    val productType: String
)
