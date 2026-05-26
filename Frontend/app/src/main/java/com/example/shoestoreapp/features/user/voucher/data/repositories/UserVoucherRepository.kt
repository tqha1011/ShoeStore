package com.example.shoestoreapp.features.user.voucher.data.repositories

import com.example.shoestoreapp.features.user.voucher.data.remote.PagedUserVoucherResponse

interface UserVoucherRepository {
    suspend fun getUserVouchers(pageIndex: Int, pageSize: Int): Result<PagedUserVoucherResponse>
}