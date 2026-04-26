package com.example.shoestoreapp.features.admin.invoice.data

import com.example.shoestoreapp.features.invoice.data.mapper.toDomain
import com.example.shoestoreapp.features.invoice.data.remote.InvoiceApi
import com.example.shoestoreapp.features.invoice.data.remote.UpdateStatusRequestDto
import com.example.shoestoreapp.features.invoice.model.Detail
import com.example.shoestoreapp.features.invoice.model.Invoice

class AdminInvoiceRepository ( private val api : InvoiceApi) {
    // get all invoices
    suspend fun getAllInvoicesAdmin(): Result<List<Invoice>> = runCatching {
        val response = api.getAllInvoicesAdmin(pageNumber = 1, pageSize = 10)
        if (response.isSuccessful) {
            val dtos = response.body()?.data?.itemDto ?: emptyList()
            dtos.map { it.toDomain() }
        } else {
            throw Exception("Invoice API error: ${response.code()} - ${response.message()}")
        }
    }

    // get invoice details
    suspend fun getInvoiceDetailsAdmin(invoiceGuid: String): Result<List<Detail>> = runCatching {
        val response = api.getInvoiceDetailsAdmin(invoiceGuid)
        if (response.isSuccessful) {
            val dtos = response.body()?.detailDto ?: emptyList()
            dtos.map { it.toDomain() }
        } else {
            throw Exception("Invoice details API error: ${response.code()} - ${response.message()}")
        }
    }

    // get invoice details
    suspend fun updateInvoiceStatusAdmin(invoiceGuid: String, status: String): Result<Unit> = runCatching {
        val request = UpdateStatusRequestDto(status = status)
        val response = api.updateInvoiceStatusAdmin(invoiceGuid, request)
        if (!response.isSuccessful) {
            val backendMessage = response.errorBody()?.string()?.takeIf { it.isNotBlank() }
            val message = backendMessage ?: response.message()
            throw Exception("Update status API error: ${response.code()} - $message")
        }
    }
}
