package com.example.shoestoreapp.features.invoice.data.mapper

import com.example.shoestoreapp.features.invoice.data.remote.DetailDto
import com.example.shoestoreapp.features.invoice.data.remote.ItemDto
import com.example.shoestoreapp.features.invoice.model.Detail
import com.example.shoestoreapp.features.invoice.model.Invoice
import com.example.shoestoreapp.features.invoice.model.toInvoiceStatus

fun ItemDto.toDomain() : Invoice {
    return Invoice(
        publicId = this.publicId ?: "",
        orderCode = this.orderCode ?: "",
        userName = this.username ?: "Empty",
        paymentMethod =  this.paymentName,
        status = this.status.toInvoiceStatus(),
        createdAt = this.dateCreated,
        finalPrice = this.finalPrice,
        address = this.address,
        phones = this.phone
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