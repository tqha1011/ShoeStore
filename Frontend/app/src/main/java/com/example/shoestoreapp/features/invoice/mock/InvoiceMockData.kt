package com.example.shoestoreapp.features.invoice.mock

import com.example.shoestoreapp.features.invoice.model.Invoice
import com.example.shoestoreapp.features.invoice.model.InvoiceDetail
import com.example.shoestoreapp.features.invoice.model.InvoiceStatus
import com.example.shoestoreapp.features.invoice.model.PaymentMethod

object InvoiceMockData {
    fun invoices(): List<Invoice> {
        val now = System.currentTimeMillis()

        return listOf(
            Invoice(
                id = 1,
                publicId = "e7ee9247-6d89-4e2e-9b78-3f89db40a111",
                userId = 1,
                fullName = "Marcus Alexander",
                status = InvoiceStatus.PENDING,
                paymentMethod = PaymentMethod.ONLINE,
                paymentId = 101,
                orderCode = "ORDER-12345",
                shippingFee = 5.0,
                finalPrice = 284.0,
                shippingAddress = "12 Nguyen Hue, District 1, HCM",
                phone = "0901234567",
                createdAtMillis = now - (45L * 60L * 1000L),
                createdAt = "May 24, 2024 - 10:42 AM",
                updatedAt = "May 24, 2024 - 10:42 AM",
                invoiceDetails = listOf(
                    InvoiceDetail(11, "detail-11", 1, 301, 1, 120.0),
                    InvoiceDetail(12, "detail-12", 1, 302, 2, 79.5)
                )
            ),
            Invoice(
                id = 2,
                publicId = "a2d2f4d5-f67f-4c63-b9bd-9ac4fef0a222",
                userId = 2,
                fullName = "Elena Rodriguez",
                status = InvoiceStatus.PAID,
                paymentMethod = PaymentMethod.ONLINE,
                paymentId = 102,
                orderCode = "ORDER-12346",
                shippingFee = 6.5,
                finalPrice = 156.5,
                shippingAddress = "88 Le Loi, District 1, HCM",
                phone = "0902222333",
                createdAtMillis = now - (20L * 60L * 1000L),
                createdAt = "May 23, 2024 - 03:15 PM",
                updatedAt = "May 23, 2024 - 03:20 PM",
                invoiceDetails = listOf(
                    InvoiceDetail(21, "detail-21", 2, 305, 1, 150.0)
                )
            ),
            Invoice(
                id = 3,
                publicId = "7f95f9e7-1bc1-4b17-9e6f-dc35a52b3333",
                userId = 1,
                fullName = "James Sterling",
                status = InvoiceStatus.DELIVERING,
                paymentMethod = PaymentMethod.COD,
                paymentId = 103,
                orderCode = "ORDER-12347",
                shippingFee = 10.0,
                finalPrice = 420.0,
                shippingAddress = "5 Tran Hung Dao, District 5, HCM",
                phone = "0918888999",
                createdAtMillis = now - (3L * 60L * 60L * 1000L),
                createdAt = "May 22, 2024 - 09:00 AM",
                updatedAt = "May 22, 2024 - 11:00 AM",
                invoiceDetails = listOf(
                    InvoiceDetail(31, "detail-31", 3, 310, 2, 180.0),
                    InvoiceDetail(32, "detail-32", 3, 311, 1, 50.0)
                )
            ),
            Invoice(
                id = 4,
                publicId = "53b8f841-39fd-4a8f-bcf8-f9c51a1c4444",
                userId = 3,
                fullName = "Sarah Chen",
                status = InvoiceStatus.DELIVERED,
                paymentMethod = PaymentMethod.COD,
                paymentId = 104,
                orderCode = "ORDER-12348",
                shippingFee = 0.0,
                finalPrice = 89.0,
                shippingAddress = "17 Pasteur, District 3, HCM",
                phone = "0981111222",
                createdAtMillis = now - (8L * 60L * 60L * 1000L),
                createdAt = "May 20, 2024 - 11:30 PM",
                updatedAt = "May 21, 2024 - 07:20 AM",
                invoiceDetails = listOf(
                    InvoiceDetail(41, "detail-41", 4, 318, 1, 89.0)
                )
            ),
            Invoice(
                id = 5,
                publicId = "8e2d4c68-35ce-41f7-bfe3-8842c6305555",
                userId = 1,
                fullName = "Oliver Brown",
                status = InvoiceStatus.CANCELED,
                paymentMethod = PaymentMethod.ONLINE,
                paymentId = 105,
                orderCode = "ORDER-12349",
                shippingFee = 0.0,
                finalPrice = 132.0,
                shippingAddress = "26 Nguyen Trai, District 1, HCM",
                phone = "0903444555",
                createdAtMillis = now - (26L * 60L * 60L * 1000L),
                createdAt = "May 19, 2024 - 08:10 PM",
                updatedAt = "May 19, 2024 - 08:45 PM",
                invoiceDetails = listOf(
                    InvoiceDetail(51, "detail-51", 5, 325, 1, 132.0)
                )
            )
        )
    }
}

