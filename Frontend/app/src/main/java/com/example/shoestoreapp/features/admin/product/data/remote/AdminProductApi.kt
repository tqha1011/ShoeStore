package com.example.shoestoreapp.features.admin.product.data.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
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

    @POST("/api/admin/products")
    suspend fun createProduct(
        @Body request: CreateProductDto
    ): Response<Unit>

    @GET("/api/admin/products/{productGuid}")
    suspend fun getProductById(
        @Path("productGuid") productId: String
    ): Response<ProductResponseDto>

    @PUT("/api/admin/products/{productGuid}")
    suspend fun updateProduct(
        @Path("productGuid") productId: String,
        @Body body: UpdateProductDto
    ): Response<ProductResponseDto>

    @DELETE("/api/admin/products/{productGuid}")
    suspend fun deleteProduct(
        @Path("productGuid") productId: String
    ): Response<Unit>

    @Multipart
    @POST("/api/products/{productGuid}/variants")
    suspend fun createVariant(
        @Path("productGuid") productId: String,
        @Part("SizeId") sizeId: RequestBody,
        @Part("ColorId") colorId: RequestBody,
        @Part("Stock") stock: RequestBody,
        @Part("Price") price: RequestBody,
        @Part("IsSelling") isSelling: RequestBody,
        @Part("ImageUrl") imageUrl: RequestBody,
        @Part image: MultipartBody.Part?
    ): Response<ProductVariantResponseDto>

    @Multipart
    @PUT("/api/products/{productGuid}/variants/{variantGuid}")
    suspend fun updateVariant(
        @Path("productGuid") productId: String,
        @Path("variantGuid") variantId: String,
        @Part("SizeId") sizeId: RequestBody,
        @Part("ColorId") colorId: RequestBody,
        @Part("Stock") stock: RequestBody,
        @Part("Price") price: RequestBody,
        @Part("IsSelling") isSelling: RequestBody,
        @Part("ImageUrl") imageUrl: RequestBody,
        @Part image: MultipartBody.Part?
    ): Response<Unit>

    @DELETE("/api/products/{productGuid}/variants/{variantGuid}")
    suspend fun deleteVariant(
        @Path("productGuid") productId: String,
        @Path("variantGuid") variantId: String
    ): Response<Unit>
}