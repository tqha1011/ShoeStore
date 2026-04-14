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
        @Query("Keyword") keyword: String?,
        @Query("inStock") inStock: Boolean?,
        @Query("outOfStock") outOfStock: Boolean?,
        @Query("lowStock") lowStock: Boolean?,
        @Query("pageIndex") pageIndex: Int?,
        @Query("pageSize") pageSize: Int?
    ): Response<ProductSearchResponse>

    @GET("/api/admin/products/{productGuid}")
    suspend fun adminSearchDetail(
        @Path("productGuid") productGuid: String
    ): Response<ProductSearchDto>

    @PUT("/api/admin/products/{productGuid}")
    suspend fun adminUpdateProduct(
        @Path("productGuid") productGuid: String,
        @Query("productName") productName: String?,
        @Query("variants") variants: List<ProductVariantDto?>
    ): Response<Unit>

    @DELETE("/api/admin/products/{productGuid}")
    suspend fun adminDeleteProduct(
        @Path("productGuid") productGuid: String
    ): Response<Unit>

    @POST("/api/admin/products")
    suspend fun adminCreateProduct(
        @Query("productName") productName: String?,
        @Query("variants") variants: List<ProductVariantDto?>
    ): Response<Unit>

}