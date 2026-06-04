package com.example.shoestoreapp.features.admin.voucher.data.repositories

import com.example.shoestoreapp.features.admin.voucher.data.remote.CreateVoucherDto
import com.example.shoestoreapp.features.admin.voucher.data.remote.UpdateVoucherDto
import com.example.shoestoreapp.features.admin.voucher.data.remote.VoucherSearchResponseDto

interface VoucherRepository {
    suspend fun createVoucher(dto: CreateVoucherDto): Result<Unit>
    suspend fun getVouchers(pageIndex: Int, pageSize: Int): Result<VoucherSearchResponseDto>
    suspend fun updateVoucher(voucherGuid: String, dto: UpdateVoucherDto): Result<Unit>
    suspend fun deleteVoucher(voucherGuid: String): Result<Unit>
    suspend fun deleteExpiredVouchers(): Result<Unit>
}

sealed class VoucherRepositoryException(message: String) : Exception(message) {
    class BadRequest(message: String = ERROR_BAD_REQUEST) : VoucherRepositoryException(message)
    class Unauthorized(message: String = ERROR_UNAUTHORIZED) : VoucherRepositoryException(message)
    class NotFound(message: String = ERROR_NOT_FOUND) : VoucherRepositoryException(message)
    class ServerError(message: String = ERROR_SERVER) : VoucherRepositoryException(message)
    class Unknown(message: String = ERROR_UNKNOWN) : VoucherRepositoryException(message)

    companion object {
        const val ERROR_BAD_REQUEST = "Invalid voucher data. Please check your input."
        const val ERROR_UNAUTHORIZED = "Unauthorized. Please sign in again."
        const val ERROR_NOT_FOUND = "Voucher not found."
        const val ERROR_SERVER = "Server error. Please try again later."
        const val ERROR_UNKNOWN = "Unable to process voucher right now."
    }
}