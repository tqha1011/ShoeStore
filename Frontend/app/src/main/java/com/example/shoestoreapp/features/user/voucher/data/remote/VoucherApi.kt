package com.example.shoestoreapp.features.user.voucher.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface VoucherApi {
    @GET("/api/admin/vouchers/valid-voucher")
    suspend fun getValidVouchers(
        @Query("pageIndex") pageIndex: Int,
        @Query("pageSize") pageSize: Int
    ): Response<PaginatedResponse<VoucherDto>>

    @POST("/api/user/vouchers/claim-voucher")
    suspend fun claimVoucher(
        @Query("publicVoucherId") voucherId: String
    ): Response<Unit>

    @GET("/api/user/vouchers")
    suspend fun getMyVouchers(
        @Query("pageIndex") pageIndex: Int,
        @Query("pageSize") pageSize: Int
    ): Response<PaginatedResponse<VoucherUserDto>>
}
