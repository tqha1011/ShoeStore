package com.example.shoestoreapp.features.admin.crud.data.remote

import com.example.shoestoreapp.features.admin.crud.data.remote.master_data.CategoryDto
import com.example.shoestoreapp.features.admin.crud.data.remote.master_data.ColorDto
import com.example.shoestoreapp.features.admin.crud.data.remote.master_data.SizeDto
import retrofit2.Response
import retrofit2.http.GET

interface MasterDataApi {
    @GET("api/master-data/sizes")
    suspend fun getSizes(): Response<List<SizeDto>>

    @GET("api/master-data/colors")
    suspend fun getColors(): Response<List<ColorDto>>

    @GET("api/master-data/categories")
    suspend fun getCategories(): Response<List<CategoryDto>>
}