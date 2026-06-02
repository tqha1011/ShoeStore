package com.example.shoestoreapp.features.user.invoice.data

import com.example.shoestoreapp.features.invoice.data.mapper.toDomain
import com.example.shoestoreapp.features.invoice.data.remote.InvoiceApi
import com.example.shoestoreapp.features.invoice.data.remote.UpdateStatusRequestDto
import com.example.shoestoreapp.features.invoice.model.Detail
import com.example.shoestoreapp.features.invoice.model.Invoice

class UserInvoiceRepository (private val api : InvoiceApi) {

    // Fetches user invoices and maps DTO to domain model.
    suspend fun getAllInvoicesUser(): Result<List<Invoice>> = runCatching {
        val response = api.getAllInvoicesUser(pageNumber = 1, pageSize = 50)
        if (response.isSuccessful) {
            val dtos = response.body()?.data?.itemDto ?: emptyList()
            dtos.map { it.toDomain() }
        } else {
            throw Exception("Invoice API error: ${response.code()} - ${response.message()}")
        }
    }

    // Fetches all line items for a single invoice.
    suspend fun getInvoiceDetailsUser(invoiceGuid: String): Result<List<Detail>> = runCatching {
        val response = api.getInvoiceDetailsUser(invoiceGuid)
        if (response.isSuccessful) {
            val dtos = response.body()?.detailDto ?: emptyList()
            dtos.map { it.toDomain() }
        } else {
            throw Exception("Invoice details API error: ${response.code()} - ${response.message()}")
        }
    }

    // Sends status update request for a specific user invoice.
    suspend fun updateInvoiceStatusUser(invoiceGuid: String, status: String): Result<Unit> = runCatching {
        val request = UpdateStatusRequestDto(status = status)
        val response = api.updateInvoiceStatusUser(invoiceGuid, request)
        if (!response.isSuccessful) {
            val backendMessage = response.errorBody()?.string()?.takeIf { it.isNotBlank() }
            val message = backendMessage ?: response.message()
            throw Exception("Update status API error: ${response.code()} - $message")
        }
    }
}