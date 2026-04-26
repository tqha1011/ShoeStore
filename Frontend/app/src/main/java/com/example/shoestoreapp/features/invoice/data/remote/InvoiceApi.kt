package com.example.shoestoreapp.features.invoice.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface InvoiceApi {
    // get all invoices
    @GET ("api/invoice/admin/get-all")
    suspend fun getAllInvoicesAdmin(
        @Query("PageNumber") pageNumber: Int = 1,
        @Query("PageSize") pageSize: Int = 10
        ): Response<BaseResponseListDto<InvoiceListDto>>
    @GET ("api/invoice/user/get-all")
    suspend fun getAllInvoicesUser(
        @Query("PageNumber") pageNumber: Int = 1,
        @Query("PageSize") pageSize: Int = 10
    ): Response<BaseResponseListDto<InvoiceListDto>>

    // get invoice details
    @GET ("api/invoice/admin/{invoiceGuid}/details")
    suspend fun getInvoiceDetailsAdmin(
        @Path ("invoiceGuid") invoiceGuid: String,
    ): Response<InvoiceDetailsDto>
    @GET ("api/invoice/user/{invoiceGuid}/details")
    suspend fun getInvoiceDetailsUser(
        @Path ("invoiceGuid") invoiceGuid: String,
    ): Response<InvoiceDetailsDto>

    //update status of invoice
    @PUT("api/invoice/admin/{invoiceGuid}/status")
    suspend fun updateInvoiceStatusAdmin(
        @Path ("invoiceGuid") invoiceGuid: String,
        @Body request: UpdateStatusRequestDto
    ): Response<UpdateStatusResponseDto>
    @PUT("api/invoice/user/{invoiceGuid}/status")
    suspend fun updateInvoiceStatusUser(
        @Path ("invoiceGuid") invoiceGuid: String,
        @Body request: UpdateStatusRequestDto
    ): Response<UpdateStatusResponseDto>
}