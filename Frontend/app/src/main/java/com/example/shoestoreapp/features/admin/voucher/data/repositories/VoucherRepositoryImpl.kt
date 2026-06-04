package com.example.shoestoreapp.features.admin.voucher.data.repositories

import com.example.shoestoreapp.core.networks.RetrofitInstance
import com.example.shoestoreapp.core.utils.ApiErrorHandler
import com.example.shoestoreapp.features.admin.voucher.data.remote.AdminVoucherApi
import com.example.shoestoreapp.features.admin.voucher.data.remote.CreateVoucherDto
import com.example.shoestoreapp.features.admin.voucher.data.remote.UpdateVoucherDto
import com.example.shoestoreapp.features.admin.voucher.data.remote.VoucherSearchResponseDto
import retrofit2.Response

class VoucherRepositoryImpl(
    private val api: AdminVoucherApi = RetrofitInstance.adminVoucherApi
) : VoucherRepository {

    override suspend fun createVoucher(dto: CreateVoucherDto): Result<Unit> {
        return try {
            val response = api.createVoucher(dto)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(response.toRepositoryException())
        } catch (e: Exception) {
            Result.failure(VoucherRepositoryException.Unknown(e.message ?: VoucherRepositoryException.ERROR_UNKNOWN))
        }
    }

    override suspend fun getVouchers(pageIndex: Int, pageSize: Int): Result<VoucherSearchResponseDto> {
        return try {
            val response = api.getVouchers(pageIndex, pageSize)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(VoucherRepositoryException.Unknown("Empty response body."))
            } else {
                Result.failure(response.toRepositoryException())
            }
        } catch (e: Exception) {
            Result.failure(VoucherRepositoryException.Unknown(e.message ?: VoucherRepositoryException.ERROR_UNKNOWN))
        }
    }

    override suspend fun updateVoucher(voucherGuid: String, dto: UpdateVoucherDto): Result<Unit> {
        return try {
            val response = api.updateVoucher(voucherGuid, dto)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(response.toRepositoryException())
        } catch (e: Exception) {
            Result.failure(VoucherRepositoryException.Unknown(e.message ?: VoucherRepositoryException.ERROR_UNKNOWN))
        }
    }

    override suspend fun deleteVoucher(voucherGuid: String): Result<Unit> {
        return try {
            val response = api.deleteVoucher(voucherGuid)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(response.toRepositoryException())
        } catch (e: Exception) {
            Result.failure(VoucherRepositoryException.Unknown(e.message ?: VoucherRepositoryException.ERROR_UNKNOWN))
        }
    }

    override suspend fun deleteExpiredVouchers(): Result<Unit> {
        return try {
            val response = api.deleteExpiredVouchers()
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(response.toRepositoryException())
        } catch (e: Exception) {
            Result.failure(VoucherRepositoryException.Unknown(e.message ?: VoucherRepositoryException.ERROR_UNKNOWN))
        }
    }

    private fun <T> Response<T>.toRepositoryException(): VoucherRepositoryException {
        val backendMessage = ApiErrorHandler.extractErrorMessage(this)

        return when (code()) {
            400 -> VoucherRepositoryException.BadRequest(backendMessage ?: VoucherRepositoryException.ERROR_BAD_REQUEST)
            401 -> VoucherRepositoryException.Unauthorized(backendMessage ?: VoucherRepositoryException.ERROR_UNAUTHORIZED)
            404 -> VoucherRepositoryException.NotFound(backendMessage ?: VoucherRepositoryException.ERROR_NOT_FOUND)
            500 -> VoucherRepositoryException.ServerError(backendMessage ?: VoucherRepositoryException.ERROR_SERVER)
            else -> VoucherRepositoryException.Unknown(backendMessage ?: "${VoucherRepositoryException.ERROR_UNKNOWN} (HTTP ${code()})")
        }
    }
}