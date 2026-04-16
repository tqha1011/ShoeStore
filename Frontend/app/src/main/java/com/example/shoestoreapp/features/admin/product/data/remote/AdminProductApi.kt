package com.example.shoestoreapp.features.admin.product.data.remote


import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query


interface AdminProductApi {
    @GET("/api/admin/products/search")
    suspend fun adminSearchProducts(
        @Query("KeyWord") keyword: String?,
        @Query("InStock") inStock: Boolean?,
        @Query("OutOfStock") outOfStock: Boolean?,
        @Query("LowStock") lowStock: Boolean?,
        @Query("PageIndex") pageIndex: Int?,
        @Query("PageSize") pageSize: Int?
    ): Response<ProductSearchResponse>

    @GET("/api/admin/products/{productGuid}")
    suspend fun adminSearchDetail(
        @Path("productGuid") productGuid: String
    ): Response<ProductSearchDto>
}