package com.example.shoestoreapp.features.invoice.model

enum class InvoiceStatus {
    PENDING,
    PAID,
    DELIVERING,
    DELIVERED,
    CANCELED
}

enum class PaymentMethod {
    ONLINE,
    COD
}

data class InvoiceDetail(
    val id: Int,
    val publicId: String,
    val invoiceId: Int,
    val productVariantId: Int,
    val quantity: Int,
    val unitPrice: Double
)

data class Invoice(
    val id: Int,
    val publicId: String,
    val userId: Int,
    val fullName: String,
    val status: InvoiceStatus,
    val paymentMethod: PaymentMethod,
    val paymentId: Int,
    val orderCode: String,
    val shippingFee: Double,
    val finalPrice: Double,
    val shippingAddress: String,
    val phone: String,
    val createdAtMillis: Long,
    val createdAt: String,
    val updatedAt: String,
    val invoiceDetails: List<InvoiceDetail>
)

private const val ONLINE_PAYMENT_TIMEOUT_MILLIS = 30L * 60L * 1000L

fun Invoice.shouldAutoCancel(nowMillis: Long = System.currentTimeMillis()): Boolean {
    return paymentMethod == PaymentMethod.ONLINE &&
        status == InvoiceStatus.PENDING &&
        nowMillis - createdAtMillis >= ONLINE_PAYMENT_TIMEOUT_MILLIS
}

fun Invoice.nextWorkflowStatus(): InvoiceStatus? {
    return when (paymentMethod) {
        PaymentMethod.ONLINE -> when (status) {
            InvoiceStatus.PENDING -> InvoiceStatus.PAID
            InvoiceStatus.PAID -> InvoiceStatus.DELIVERING
            InvoiceStatus.DELIVERING -> InvoiceStatus.DELIVERED
            InvoiceStatus.DELIVERED, InvoiceStatus.CANCELED -> null
        }

        PaymentMethod.COD -> when (status) {
            InvoiceStatus.PENDING -> InvoiceStatus.DELIVERING
            InvoiceStatus.DELIVERING -> InvoiceStatus.PAID
            InvoiceStatus.PAID -> InvoiceStatus.DELIVERED
            InvoiceStatus.DELIVERED, InvoiceStatus.CANCELED -> null
        }
    }
}

