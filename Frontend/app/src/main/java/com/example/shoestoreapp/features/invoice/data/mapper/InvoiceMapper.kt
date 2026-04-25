package com.example.shoestoreapp.features.invoice.data.mapper

import com.example.shoestoreapp.features.invoice.data.remote.DetailDto
import com.example.shoestoreapp.features.invoice.data.remote.ItemDto
import com.example.shoestoreapp.features.invoice.model.Detail
import com.example.shoestoreapp.features.invoice.model.Invoice
import com.example.shoestoreapp.features.invoice.model.InvoiceStatus

fun ItemDto.toDomain() : Invoice {
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

fun DetailDto.toDomain() : Detail {
    return Detail(
        color = this.color ?: "",
        imageUrl = this.imageUrl ?: "",
        productName = this.productName ?: "",
        quantity = this.quantity ?: 0,
        size = this.size ?: 0,
        unitPrice = this.unitPrice ?: 0
    )
}