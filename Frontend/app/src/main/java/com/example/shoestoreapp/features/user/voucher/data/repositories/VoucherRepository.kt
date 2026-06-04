package com.example.shoestoreapp.features.user.voucher.data.repositories

import com.example.shoestoreapp.features.user.voucher.data.remote.PaginatedResponse
import com.example.shoestoreapp.features.user.voucher.data.remote.VoucherDto
import com.example.shoestoreapp.features.user.voucher.data.remote.VoucherUserDto

interface VoucherRepository {
    suspend fun getValidVouchers(
        pageIndex: Int,
        pageSize: Int
    ): Result<PaginatedResponse<VoucherDto>>

    suspend fun claimVoucher(voucherId: String): Result<Unit>

    suspend fun getMyVouchers(
        pageIndex: Int,
        pageSize: Int
    ): Result<PaginatedResponse<VoucherUserDto>>
}
