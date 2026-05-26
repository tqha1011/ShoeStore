package com.example.shoestoreapp.features.user.voucher.data.repositories

import com.example.shoestoreapp.core.networks.RetrofitInstance
import com.example.shoestoreapp.features.user.voucher.data.remote.PagedUserVoucherResponse
import com.example.shoestoreapp.features.user.voucher.data.remote.UserVoucherApi

class UserVoucherRepositoryImpl(
    private val api: UserVoucherApi = RetrofitInstance.userVoucherApi
) : UserVoucherRepository {
    override suspend fun getUserVouchers(pageIndex: Int, pageSize: Int): Result<PagedUserVoucherResponse> {
        return try {
            val response = api.getUserVouchers(pageIndex, pageSize)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Empty response body."))
            } else {
                Result.failure(Exception("Fetch user vouchers failed (HTTP ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}