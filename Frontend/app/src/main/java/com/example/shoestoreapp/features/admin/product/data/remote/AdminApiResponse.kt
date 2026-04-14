package com.example.shoestoreapp.features.admin.product.data.remote

import com.google.gson.annotations.SerializedName

// 1. DTO cho Request (Giữ nguyên của đại ca, rất tốt)
data class ProductSearchRequest(
    val keyword: String? = null,
    val brand: String? = null,
    val productId: Int? = null,
    val listColorId: List<String>? = null,
    val listSizeId: List<String>? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val sort: String? = null,
    val pageIndex: Int = 1,
    val pageSize: Int = 10
) {
    fun toMap(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        keyword?.let { map["Keyword"] = it }
        brand?.let { map["Brand"] = it }
        productId?.let { map["ProductId"] = it.toString() }
        listColorId?.let { if (it.isNotEmpty()) map["ListColorId"] = it.joinToString(",") }
        listSizeId?.let { if (it.isNotEmpty()) map["ListSizeId"] = it.joinToString(",") }
        minPrice?.let { map["MinPrice"] = it.toString() }
        maxPrice?.let { map["MaxPrice"] = it.toString() }
        sort?.let { map["Sort"] = it }
        map["PageIndex"] = pageIndex.toString()
        map["PageSize"] = pageSize.toString()
        return map
    }
}

// 2. DTO cho Response tổng quát
data class ProductSearchResponse(
    @SerializedName("totalCount") val totalCount: Int,
    @SerializedName("pageNumber") val pageNumber: Int,
    @SerializedName("pageSize") val pageSize: Int,
    @SerializedName("totalPages") val totalPages: Int,
    @SerializedName("hasPrevious") val hasPrevious: Boolean,
    @SerializedName("hasNext") val hasNext: Boolean,
    @SerializedName("items") val items: List<ProductSearchDto>
)

// 3. DTO cho từng sản phẩm
data class ProductSearchDto(
    @SerializedName("publicId") val publicId: String?,
    @SerializedName("productName") val productName: String?,
    @SerializedName("brand") val brand: String?,
    @SerializedName("variants") val variants: List<ProductVariantDto>?
)

// 4. DTO cho biến thể sản phẩm
data class ProductVariantDto(
    @SerializedName("publicId") val publicId: String?,
    @SerializedName("sizeId") val sizeId: Int?,
    @SerializedName("size") val size: Int?,
    @SerializedName("colorId") val colorId: Int?,
    @SerializedName("colorName") val colorName: String?,
    @SerializedName("stock") val stock: Int?,
    @SerializedName("price") val price: Double?,
    @SerializedName("imageUrl") val imageUrl: String?,
    @SerializedName("isSelling") val isSelling: Boolean?,
    @SerializedName("isDelete") val isDelete: Boolean?
)