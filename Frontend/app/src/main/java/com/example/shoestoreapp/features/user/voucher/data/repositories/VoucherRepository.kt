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

sealed class UserVoucherRepositoryException(message: String) : Exception(message) {
    class BadRequest(message: String = ERROR_BAD_REQUEST) : UserVoucherRepositoryException(message)
    class Unauthorized(message: String = ERROR_UNAUTHORIZED) : UserVoucherRepositoryException(message)
    class NotFound(message: String = ERROR_NOT_FOUND) : UserVoucherRepositoryException(message)
    class ServerError(message: String = ERROR_SERVER) : UserVoucherRepositoryException(message)
    class Unknown(message: String = ERROR_UNKNOWN) : UserVoucherRepositoryException(message)

    companion object {
        const val ERROR_BAD_REQUEST = "Invalid request. Please try again."
        const val ERROR_UNAUTHORIZED = "Unauthorized. Please sign in again."
        const val ERROR_NOT_FOUND = "Voucher not found."
        const val ERROR_SERVER = "Server error. Please try again later."
        const val ERROR_UNKNOWN = "Something went wrong with the voucher service."
    }
}