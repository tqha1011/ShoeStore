package com.example.shoestoreapp.features.user.invoice.data

import com.example.shoestoreapp.features.invoice.data.mapper.toDomain
import com.example.shoestoreapp.features.invoice.data.remote.InvoiceApi
import com.example.shoestoreapp.features.invoice.data.remote.UpdateStatusRequestDto
import com.example.shoestoreapp.features.invoice.model.Detail
import com.example.shoestoreapp.features.invoice.model.Invoice

class UserInvoiceRepository (private val api : InvoiceApi) {

//    suspend fun getAllInvoicesUser(): Result<List<Invoice>> = runCatching {
//        val response = api.getAllInvoicesUser(pageNumber = 1, pageSize = 10)
//        if (response.isSuccessful) {
//            val dtos = response.body()?.itemDto ?: emptyList()
//            dtos.map { it.toDomain() }
//        } else {
//            throw Exception("Lỗi API: ${response.code()} - ${response.message()}")
//        }
//    }

    suspend fun getInvoiceDetailsUser(invoiceGuid: String): Result<List<Detail>> = runCatching {
        val response = api.getInvoiceDetailsUser(invoiceGuid)
        if (response.isSuccessful) {
            val dtos = response.body()?.detailDto ?: emptyList()
            dtos.map { it.toDomain() }
        } else {
            throw Exception("Lỗi : ${response.code()} - ${response.message()}")
        }
    }


    suspend fun updateInvoiceStatusUser(invoiceGuid: String, statusId: Int): Result<Unit> = runCatching {
        val request = UpdateStatusRequestDto(status = statusId)
        val response = api.updateInvoiceStatusUser(invoiceGuid, request)
        if (!response.isSuccessful) {
            throw Exception("Lỗi update : ${response.code()} - ${response.message()}")
        }
    }
}