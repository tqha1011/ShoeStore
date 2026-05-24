package com.example.shoestoreapp.features.admin.voucher.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AdminVoucherApi {
    @POST("/api/admin/vouchers")
    suspend fun createVoucher(
        @Body request: CreateVoucherDto
    ): Response<Unit>

    @GET("/api/admin/vouchers")
    suspend fun getVouchers(
        @Query("pageIndex") pageIndex: Int = 1,
        @Query("pageSize") pageSize: Int = 10
    ): Response<VoucherSearchResponseDto>
}
