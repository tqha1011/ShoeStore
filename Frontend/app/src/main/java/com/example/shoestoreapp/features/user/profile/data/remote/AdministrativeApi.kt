package com.example.shoestoreapp.features.user.profile.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AdministrativeApi {
    @GET("api/")
    suspend fun getProvinces(@Query("depth") depth: Int = 1): Response<List<ProvinceDto>>

    @GET("api/p/{provinceCode}")
    suspend fun getProvinceDetail(
        @Path("provinceCode") provinceCode: Int,
        @Query("depth") depth: Int = 2
    ): Response<ProvinceDto>

    @GET("api/d/{districtCode}")
    suspend fun getDistrictDetail(
        @Path("districtCode") districtCode: Int,
        @Query("depth") depth: Int = 2
    ): Response<DistrictDto>
}