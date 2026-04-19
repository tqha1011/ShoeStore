package com.example.shoestoreapp.features.admin.crud.data.remote

import com.example.shoestoreapp.features.admin.crud.data.remote.master_data.CategoryDto
import com.example.shoestoreapp.features.admin.crud.data.remote.master_data.ColorDto
import com.example.shoestoreapp.features.admin.crud.data.remote.master_data.SizeDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query


interface AdminProductCrudApi {
    @PUT("/api/admin/products/{productGuid}")
    suspend fun adminUpdateProduct(
        @Path("productGuid") productGuid: String,
        @Body request: ProductUpdateDtoRequest
    ): Response<Unit>

    @DELETE("/api/admin/products/{productGuid}")
    suspend fun adminDeleteProduct(
        @Path("productGuid") productGuid: String
    ): Response<Unit>

    @POST("/api/admin/products")
    suspend fun adminCreateProduct(
        @Body request: ProductCreateDtoRequest
    ): Response<Unit>
}
