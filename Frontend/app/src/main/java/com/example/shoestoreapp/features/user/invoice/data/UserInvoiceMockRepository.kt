package com.example.shoestoreapp.features.user.invoice.data

import com.example.shoestoreapp.features.invoice.data.InvoiceMockData
import com.example.shoestoreapp.features.invoice.model.Invoice

class UserInvoiceMockRepository {
    fun getInvoicesByUserName(userName: String): List<Invoice> {
        return InvoiceMockData.invoices().filter { it.userName.equals(userName, ignoreCase = true) }
    }
}
