package com.example.shoestoreapp.features.user.voucher.data.repositories

import com.example.shoestoreapp.core.networks.RetrofitInstance
import com.example.shoestoreapp.core.utils.ApiErrorHandler
import com.example.shoestoreapp.features.user.voucher.data.remote.PaginatedResponse
import com.example.shoestoreapp.features.user.voucher.data.remote.VoucherApi
import com.example.shoestoreapp.features.user.voucher.data.remote.VoucherDto
import com.example.shoestoreapp.features.user.voucher.data.remote.VoucherUserDto
import retrofit2.Response

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
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(UserVoucherRepositoryException.Unknown("Empty response body."))
            } else {
                Result.failure(response.toRepositoryException())
            }
        } catch (e: Exception) {
            Result.failure(UserVoucherRepositoryException.Unknown(e.message ?: UserVoucherRepositoryException.ERROR_UNKNOWN))
        }
    }

    override suspend fun claimVoucher(voucherId: String): Result<Unit> {
        return try {
            val response = api.claimVoucher(voucherId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(response.toRepositoryException())
            }
        } catch (e: Exception) {
            Result.failure(UserVoucherRepositoryException.Unknown(e.message ?: UserVoucherRepositoryException.ERROR_UNKNOWN))
        }
    }

    override suspend fun getMyVouchers(
        pageIndex: Int,
        pageSize: Int
    ): Result<PaginatedResponse<VoucherUserDto>> {
        return try {
            val response = api.getMyVouchers(pageIndex, pageSize)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(UserVoucherRepositoryException.Unknown("Empty response body."))
            } else {
                Result.failure(response.toRepositoryException())
            }
        } catch (e: Exception) {
            Result.failure(UserVoucherRepositoryException.Unknown(e.message ?: UserVoucherRepositoryException.ERROR_UNKNOWN))
        }
    }

    // ================
    // LOGIC PARSE LỖI
    // ================

    private fun <T> Response<T>.toRepositoryException(): UserVoucherRepositoryException {
        val backendMessage = ApiErrorHandler.extractErrorMessage(this)

        return when (code()) {
            400 -> UserVoucherRepositoryException.BadRequest(backendMessage ?: UserVoucherRepositoryException.ERROR_BAD_REQUEST)
            401 -> UserVoucherRepositoryException.Unauthorized(backendMessage ?: UserVoucherRepositoryException.ERROR_UNAUTHORIZED)
            404 -> UserVoucherRepositoryException.NotFound(backendMessage ?: UserVoucherRepositoryException.ERROR_NOT_FOUND)
            500 -> UserVoucherRepositoryException.ServerError(backendMessage ?: UserVoucherRepositoryException.ERROR_SERVER)
            else -> UserVoucherRepositoryException.Unknown(backendMessage ?: "${UserVoucherRepositoryException.ERROR_UNKNOWN} (HTTP ${code()})")
        }
    }
}