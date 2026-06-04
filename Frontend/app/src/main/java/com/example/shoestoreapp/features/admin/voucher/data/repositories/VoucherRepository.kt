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
