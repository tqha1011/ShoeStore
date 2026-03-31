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

/**
 * Data class đại diện cho sản phẩm trong giao diện Admin.
 * Mở rộng thông tin từ Product bằng cách thêm các thông tin quản lý kho.
 *
 * @param id - ID sản phẩm
 * @param name - Tên sản phẩm (VD: "Nike Air Max 270")
 * @param imageUrl - URL ảnh sản phẩm
 * @param description - Mô tả sản phẩm (VD: "Red and Black Colorway")
 * @param price - Giá sản phẩm
 * @param stockStatus - Trạng thái tồn kho (IN_STOCK, LOW_STOCK, OUT_OF_STOCK)
 * @param quantity - Số lượng hàng còn lại trong kho
 * @param category - Danh mục sản phẩm (VD: "BESTSELLER", "NEW RELEASE")
 * @param productType - Loại sản phẩm (VD: "Men's Shoes", "Running Shoes")
 */
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
