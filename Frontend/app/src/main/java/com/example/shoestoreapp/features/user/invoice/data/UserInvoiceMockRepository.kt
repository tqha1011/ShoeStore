package com.example.shoestoreapp.features.user.invoice.data

import com.example.shoestoreapp.features.invoice.mock.InvoiceMockData
import com.example.shoestoreapp.features.invoice.model.Invoice

class UserInvoiceMockRepository {
    fun getInvoicesByUser(userId: Int): List<Invoice> {
        return InvoiceMockData.invoices().filter { it.userId == userId }
    }
}

