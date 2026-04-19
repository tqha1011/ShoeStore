package com.example.shoestoreapp.features.invoice.mock

import com.example.shoestoreapp.features.invoice.model.Invoice
import com.example.shoestoreapp.features.invoice.model.InvoiceStatus

object InvoiceMockData {
    fun invoices(): List<Invoice> {
        return listOf(
            Invoice(
                orderCode = "ORDER-12345",
                userName = "Marcus Alexander",
                paymentMethod = "SePay",
                status = InvoiceStatus.PENDING,
                createdAt = "May 24, 2024 - 10:42 AM",
                finalPrice = "$284.00",
                imageUrl = "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=400"
            ),
            Invoice(
                orderCode = "ORDER-12346",
                userName = "Elena Rodriguez",
                paymentMethod = "SePay",
                status = InvoiceStatus.PAID,
                createdAt = "May 23, 2024 - 03:15 PM",
                finalPrice = "$156.50",
                imageUrl = "https://images.unsplash.com/photo-1525966222134-fcfa99b8ae77?w=400"
            ),
            Invoice(
                orderCode = "ORDER-12347",
                userName = "James Sterling",
                paymentMethod = "COD",
                status = InvoiceStatus.DELIVERING,
                createdAt = "May 22, 2024 - 09:00 AM",
                finalPrice = "$420.00",
                imageUrl = "https://images.unsplash.com/photo-1600185365483-26d7a4cc7519?w=400"
            ),
            Invoice(
                orderCode = "ORDER-12348",
                userName = "Sarah Chen",
                paymentMethod = "COD",
                status = InvoiceStatus.DELIVERED,
                createdAt = "May 20, 2024 - 11:30 PM",
                finalPrice = "$89.00",
                imageUrl = "https://images.unsplash.com/photo-1460353581641-37baddab0fa2?w=400"
            ),
            Invoice(
                orderCode = "ORDER-12349",
                userName = "Oliver Brown",
                paymentMethod = "SePay",
                status = InvoiceStatus.CANCELED,
                createdAt = "May 19, 2024 - 08:10 PM",
                finalPrice = "$132.00",
                imageUrl = "https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?w=400"
            )
        )
    }
}
