package com.example.shoestoreapp.features.invoice.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface InvoiceApi {
    @GET ("api/invoice/admin/get-all")
    suspend fun getAllInvoices(
        @Query("PageNumber") pageNumber: Int = 1,
        @Query("PageSize") pageSize: Int = 10
        ): Response<InvoiceListDto>

    @GET ("api/invoice/user/{invoiceGuid}/details")
    suspend fun getInvoiceDetails(
        @Path ("invoiceGuid") invoiceGuid: String,
    ): Response<InvoiceDetailsDto>

    @PUT("api/invoice/user/{invoiceGuid}/status")
    suspend fun updateInvoiceStatus(
        @Path ("invoiceGuid") invoiceGuid: String,
        @Body request: UpdateStatusRequestDto
    ): Response<UpdateStatusResponseDto>
}