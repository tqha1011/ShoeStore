package com.example.shoestoreapp.features.user.product.data.remote

/**
 * ProductSearchRequest: Query parameters cho endpoint /api/products/search
 * Được gửi qua @Query parameters
 * Match với backend ProductSearchRequest
 */
data class ProductSearchRequest(
    val keyword: String? = null,
    val brand: String? = null,
    val productId: Int? = null,
    val listColorId: List<Int?>? = null,
    val listSizeId: List<Int?>? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val sort: String? = "default",
    val pageIndex: Int = 1,
    val pageSize: Int = 4
) {
    fun toMap(): Map<String, String> {
        val map = mutableMapOf<String, String>()

        keyword?.let { if (it.isNotBlank()) map["Keyword"] = it }
        brand?.let { if (it.isNotBlank()) map["Brand"] = it }
        sort?.let { map["Sort"] = it }
        productId?.let { map["ProductId"] = it.toString() }
        minPrice?.let { map["MinPrice"] = it.toString() }
        maxPrice?.let { map["MaxPrice"] = it.toString() }
        map["PageIndex"] = pageIndex.toString()
        map["PageSize"] = pageSize.toString()
        listColorId?.filterNotNull()?.let { list ->
            if (list.isNotEmpty()) map["ListColorId"] = list.joinToString(",")
        }
        listSizeId?.filterNotNull()?.let { list ->
            if (list.isNotEmpty()) map["ListSizeId"] = list.joinToString(",")
        }

        return map
    }
}

/**
 * ProductSearchResponse: Hứng response từ /api/products/search
 * Chứa pagination info và danh sách sản phẩm
 */
data class ProductSearchResponse(
    val totalCount: Int,
    val pageNumber: Int,
    val pageSize: Int,
    val items: List<ProductResponseDto>  // ← Updated to ProductResponseDto
)

data class DetailResponse<T>(
    val message: String,
    val data: T // Cái data này mới chứa thông tin đôi giày nè
)
/**
 * ProductResponseDto: Response DTO từ Backend
 * Match với Backend: { PublicId, ProductName, Brand, Variants[] }
 * Dùng cho endpoint list products và search
 */
data class ProductResponseDto(
    val publicId: String,  // UUID của sản phẩm
    val productName: String,
    val brand: String,
    val variants: List<ProductVariantDto> = emptyList()
)


/**
 * ProductVariantDto: Biến thể sản phẩm (size, color, ...)
 * Match với Backend ProductVariantResponseDto
 * Mỗi variant có giá, stock, size, color riêng
 */
data class ProductVariantDto(
    val publicId: String,       // UUID của variant
    val sizeId: Int,
    val size: Int? = null,
    val colorId: Int? = null,
    val colorName: String? = null,
    val stock: Int,
    val price: Double,          // decimal → Double
    val imageUrl: String? = null,
    val isSelling: Boolean = true,
    val isDelete: Boolean = false
)
