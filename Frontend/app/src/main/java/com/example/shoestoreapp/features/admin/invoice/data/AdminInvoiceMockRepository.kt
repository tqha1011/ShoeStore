package com.example.shoestoreapp.features.admin.invoice.data

import com.example.shoestoreapp.features.invoice.data.InvoiceMockData
import com.example.shoestoreapp.features.invoice.model.Invoice

class AdminInvoiceMockRepository {
    fun getInvoices(): List<Invoice> = InvoiceMockData.invoices()
}

