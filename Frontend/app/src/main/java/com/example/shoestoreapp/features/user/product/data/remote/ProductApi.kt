package com.example.shoestoreapp.features.user.product.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

/**
 * ProductApi: Interface định nghĩa các endpoint liên quan đến sản phẩm
 * Khớp với backend endpoints từ ProductsController
 * 
 * Chức năng:
 * - GET /api/products/search - Tìm kiếm sản phẩm với pagination, filtering, sorting
 * - GET /api/products/{productGuid} - Lấy chi tiết sản phẩm theo GUID
 */
interface ProductApi {

    /**
     * Tìm kiếm và lấy danh sách sản phẩm với pagination, filtering, sorting
     * 
     * @return Response chứa ApiResponse<ProductSearchResponse>
     * Response format: { "message": "...", "data": { "totalCount": ..., "items": [...] } }
     */
    @GET("api/products/search")
    suspend fun searchProducts(
        @QueryMap options: Map<String, String>
    ): Response<ProductSearchResponse>

    /**
     * Lấy chi tiết sản phẩm theo GUID
     * 
     * @param productGuid - Mã GUID của sản phẩm (String)
     * @return Response chứa ApiResponse<ProductDetailDto>
     * Response format: { "message": "...", "data": { "productGuid": "...", ... } }
     */
    @GET("api/products/{productGuid}")
    suspend fun getProductDetail(
        @Path("productGuid") productGuid: String
    ): Response<DetailResponse<ProductResponseDto>>
}

