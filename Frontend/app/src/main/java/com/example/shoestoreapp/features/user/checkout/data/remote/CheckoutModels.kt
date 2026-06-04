package com.example.shoestoreapp.features.user.checkout.data.remote

import com.example.shoestoreapp.features.user.checkout.data.models.OrderSummary
import com.google.gson.annotations.SerializedName

// 1. DTO thông tin từng món hàng
data class CheckOutRequestDto(
    @SerializedName("variantId") val variantId: String,
    @SerializedName("quantity") val quantity: Int
)

// 2. DTO chỉ phục vụ riêng cho API Prepare Checkout
data class PrepareCheckOutRequestDto(
    @SerializedName("checkOutList") val checkOutList: List<CheckOutRequestDto>,
    @SerializedName("voucherIds") val voucherIds: List<Int>
)

// 3. Các DTO Response của API Prepare Checkout
data class CheckOutResponseDto(
    @SerializedName("items") val items: List<CheckOutItemDto>,
    @SerializedName("summary") val summary: CheckOutSummaryDto,
    @SerializedName("warnings") val warnings: List<String>? = emptyList()
)

data class CheckOutItemDto(
    @SerializedName("colorName") val colorName: String?,
    @SerializedName("isOutOfStock") val isOutOfStock: Boolean,
    @SerializedName("productName") val productName: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("size") val size: Double,
    @SerializedName("stockAvailable") val stockAvailable: Int,
    @SerializedName("subTotal") val subTotal: Double,
    @SerializedName("unitPrice") val unitPrice: Double,
    @SerializedName("variantId") val variantId: String
)

data class CheckOutSummaryDto(
    @SerializedName("finalPrice") val finalPrice: Double,
    @SerializedName("shippingFee") val shippingFee: Double,
    @SerializedName("totalPrice") val totalPrice: Double
)

data class PlaceOrderRequestDto(
    @SerializedName("address") val address: String,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("items") val items: List<CheckOutRequestDto>,
    @SerializedName("paymentId") val paymentId: Int,
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("voucherIds") val voucherIds: List<Int>
)

data class InvoiceDto(
    @SerializedName("invoicePublicId") val invoicePublicId: String,
    @SerializedName("orderCode") val orderCode: String,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("shippingAddress") val shippingAddress: String,
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("status") val status: String,
    @SerializedName("shippingFee") val shippingFee: Double?,
    @SerializedName("finalPrice") val finalPrice: Double?,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("paymentId") val paymentId: Int,
    @SerializedName("shopBankCode") val shopBankCode: String?,
    @SerializedName("shopBankAccount") val shopBankAccount: String?,
    @SerializedName("shopAccountName") val shopAccountName: String?,
)

/**
 * Hàm mở rộng giúp chuyển đổi từ DTO của Server sang Model của UI
 */
fun CheckOutSummaryDto.toOrderSummary(): OrderSummary {
    return OrderSummary(
        subtotal = this.totalPrice,
        total = this.finalPrice,
        shipping = this.shippingFee,
    )
}