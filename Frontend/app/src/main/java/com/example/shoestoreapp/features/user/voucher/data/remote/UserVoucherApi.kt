package com.example.shoestoreapp.features.user.voucher.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface UserVoucherApi {
    @GET("/api/user/vouchers")
    suspend fun getUserVouchers(
        @Query("pageIndex") pageIndex: Int,
        @Query("pageSize") pageSize: Int
    ): Response<PagedUserVoucherResponse>
}