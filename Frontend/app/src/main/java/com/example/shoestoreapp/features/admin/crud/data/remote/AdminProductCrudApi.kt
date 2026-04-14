package com.example.shoestoreapp.features.admin.crud.data.remote

import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query


interface AdminProductCrudApi {
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
        @Query("variants") variants: List<ProductVariantDto?>,
        @Query("brand") brand: String?
    ): Response<Unit>
}