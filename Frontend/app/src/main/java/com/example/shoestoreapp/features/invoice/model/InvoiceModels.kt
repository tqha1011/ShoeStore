package com.example.shoestoreapp.features.invoice.model

enum class InvoiceStatus(val id: Int?) {
    PENDING(1),
    PAID(2),
    CANCELED(3),
    DELIVERING(4),
    DELIVERED(5)
}

data class Invoice(
    val orderCode: String,
    val userName: String,
    val paymentMethod: String?,
    val status: InvoiceStatus?,
    val createdAt: String?,
    val finalPrice: String?,
)
// Data Details
data class Detail (
    val color: String,
    val imageUrl: String,
    val productName: String,
    val quantity: Int,
    val size: Int,
    val unitPrice: Int
)

fun InvoiceStatus.displayName(): String {
    return when (this) {
        InvoiceStatus.PENDING -> "Pending"
        InvoiceStatus.PAID -> "Paid"
        InvoiceStatus.CANCELED -> "Canceled"
        InvoiceStatus.DELIVERING -> "Delivering"
        InvoiceStatus.DELIVERED -> "Delivered"
    }
}

fun Invoice.nextWorkflowStatus(): InvoiceStatus? {
    val currentStatus = status ?: return null
    return when (paymentMethod?.trim()?.uppercase()) {
        "SEPAY" -> when (currentStatus) {
            InvoiceStatus.PENDING -> InvoiceStatus.PAID
            InvoiceStatus.PAID -> InvoiceStatus.DELIVERING
            InvoiceStatus.DELIVERING -> InvoiceStatus.DELIVERED
            InvoiceStatus.DELIVERED, InvoiceStatus.CANCELED -> null
        }

        "COD" -> when (currentStatus) {
            InvoiceStatus.PENDING -> InvoiceStatus.DELIVERING
            InvoiceStatus.DELIVERING -> InvoiceStatus.PAID
            InvoiceStatus.PAID -> InvoiceStatus.DELIVERED
            InvoiceStatus.DELIVERED, InvoiceStatus.CANCELED -> null
        }

        else -> null
    }
}

fun Int?.toInvoiceStatusOrNull(): InvoiceStatus? {
    val rawValue = this ?: return null
    return InvoiceStatus.entries.firstOrNull { it.id == rawValue }
}
