package com.example.shoestoreapp.features.user.voucher.data.repositories

import com.example.shoestoreapp.core.networks.RetrofitInstance
import com.example.shoestoreapp.features.user.voucher.data.remote.PaginatedResponse
import com.example.shoestoreapp.features.user.voucher.data.remote.VoucherApi
import com.example.shoestoreapp.features.user.voucher.data.remote.VoucherDto
import com.example.shoestoreapp.features.user.voucher.data.remote.VoucherUserDto

class VoucherRepositoryImpl(
    private val api: VoucherApi = RetrofitInstance.voucherApi
) : VoucherRepository {
    override suspend fun getValidVouchers(
        pageIndex: Int,
        pageSize: Int
    ): Result<PaginatedResponse<VoucherDto>> {
        return try {
            val response = api.getValidVouchers(pageIndex, pageSize)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Result.failure(Exception("Fetch valid vouchers failed (HTTP ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun claimVoucher(voucherId: String): Result<Unit> {
        return try {
            val response = api.claimVoucher(voucherId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Claim voucher failed (HTTP ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMyVouchers(
        pageIndex: Int,
        pageSize: Int
    ): Result<PaginatedResponse<VoucherUserDto>> {
        return try {
            val response = api.getMyVouchers(pageIndex, pageSize)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Result.failure(Exception("Fetch my vouchers failed (HTTP ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
