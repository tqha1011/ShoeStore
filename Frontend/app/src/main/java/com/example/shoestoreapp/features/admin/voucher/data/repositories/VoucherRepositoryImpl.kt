package com.example.shoestoreapp.features.admin.voucher.data.repositories

import com.example.shoestoreapp.core.networks.RetrofitInstance
import com.example.shoestoreapp.features.admin.voucher.data.remote.AdminVoucherApi
import com.example.shoestoreapp.features.admin.voucher.data.remote.CreateVoucherDto
import com.example.shoestoreapp.features.admin.voucher.data.remote.UpdateVoucherDto
import com.example.shoestoreapp.features.admin.voucher.data.remote.VoucherSearchResponseDto
import retrofit2.HttpException

class VoucherRepositoryImpl(
    private val api: AdminVoucherApi = RetrofitInstance.adminVoucherApi
) : VoucherRepository {
    override suspend fun createVoucher(dto: CreateVoucherDto): Result<Unit> {
        return try {
            val response = api.createVoucher(dto)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getVouchers(pageIndex: Int, pageSize: Int): Result<VoucherSearchResponseDto> {
        return try {
            val response = api.getVouchers(pageIndex, pageSize)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateVoucher(voucherGuid: String, dto: UpdateVoucherDto): Result<Unit> {
        return try {
            val response = api.updateVoucher(voucherGuid, dto)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteVoucher(voucherGuid: String): Result<Unit> {
        return try {
            val response = api.deleteVoucher(voucherGuid)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteExpiredVouchers(): Result<Unit> {
        return try {
            val response = api.deleteExpiredVouchers()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}