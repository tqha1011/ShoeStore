package com.example.shoestoreapp.features.invoice.data.mapper

import com.example.shoestoreapp.features.invoice.data.remote.Item
import com.example.shoestoreapp.features.invoice.model.Invoice
import com.example.shoestoreapp.features.invoice.model.InvoiceStatus

fun Item.toDomain() : Invoice {
    return Invoice(
        orderCode = this.orderCode ?: "",
        userName = this.username ?: "Empty",
        paymentMethod =  this.paymentName,
        status = when (this.status){
            1 -> InvoiceStatus.PENDING
            2 -> InvoiceStatus.PAID
            3 -> InvoiceStatus.CANCELED
            4 -> InvoiceStatus.DELIVERING
            5 -> InvoiceStatus.DELIVERED
            else -> InvoiceStatus.PENDING
        },
        createdAt = this.dateCreated,
        finalPrice = this.finalPrice
    )
}