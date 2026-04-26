package com.example.shoestoreapp.features.invoice.data.repositories

import com.example.shoestoreapp.features.invoice.data.remote.InvoiceApi
import com.example.shoestoreapp.features.invoice.data.mapper.toDomain
import com.example.shoestoreapp.features.invoice.data.remote.UpdateStatusRequestDto
import com.example.shoestoreapp.features.invoice.model.Invoice
import com.example.shoestoreapp.features.invoice.model.Detail
class InvoiceRepositoryImpl (private val api : InvoiceApi) {
    suspend fun getAllInvoices(): Result<List<Invoice>> = runCatching {
        val response = api.getAllInvoices(pageNumber = 1, pageSize = 10)
        if (response.isSuccessful) {
            val dtos = response.body()?.itemDto ?: emptyList()
            dtos.map { it.toDomain() }
        } else {
            throw Exception("Lỗi API: ${response.code()} - ${response.message()}")
        }
    }

    suspend fun updateInvoiceStatus(invoiceGuid: String, status: Int): Result<Unit> = runCatching {
        val request = UpdateStatusRequestDto(status = status)
        val response = api.updateInvoiceStatus(invoiceGuid, request)
        if (!response.isSuccessful) {
            throw Exception("Lỗi update : ${response.code()} - ${response.message()}")
        }
    }

    suspend fun getInvoiceDetails(invoiceGuid: String): Result<List<Detail>> = runCatching {
        val response = api.getInvoiceDetails(invoiceGuid)
        if (response.isSuccessful) {
            val dtos = response.body()?.detailDto ?: emptyList()
            dtos.map { it.toDomain() }
        } else {
            throw Exception("Lỗi : ${response.code()} - ${response.message()}")
        }
    }
}