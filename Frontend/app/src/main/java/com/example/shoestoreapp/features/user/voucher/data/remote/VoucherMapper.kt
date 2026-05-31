package com.example.shoestoreapp.features.user.voucher.data.remote

import com.example.shoestoreapp.features.user.voucher.data.models.VoucherUiModel
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

private val discountFormat = DecimalFormat("0.##")

fun VoucherDto.toUiModel(): VoucherUiModel {
    return VoucherUiModel(
        id = voucherGuid ?: "unknown_id",
        numericId = -1,
        discountValue = formatDiscountValue(discount ?: 0.0, discountType),
        scope = mapScope(voucherScope),
        title = voucherName ?: "SPECIAL OFFER",
        description = description ?: "Apply this voucher to get discounts.",
        expiryDate = formatExpiryDate(validTo),
        isCollected = false,
        isUsed = false
    )
}

fun VoucherUserDto.toUiModel(): VoucherUiModel {
    return VoucherUiModel(
        id = voucherGuid ?: "unknown_id",
        numericId = voucherId ?: -1,
        discountValue = formatDiscountValue(discount ?: 0.0, discountType),
        scope = mapScope(voucherScope),
        title = voucherName ?: "SPECIAL OFFER",
        description = description ?: "Apply this voucher to get discounts.",
        expiryDate = formatExpiryDate(validTo),
        isCollected = true,
        isUsed = isUsed ?: false
    )
}

private fun formatDiscountValue(discount: Double, discountType: String?): String {
    return when (discountType) {
        "Percentage" -> {
            val actualPercent = if (discount > 0.0 && discount <= 1.0) {
                discount * 100
            } else {
                discount
            }
            "${discountFormat.format(actualPercent)}% OFF"
        }
        "FixedAmount" -> {
            val formatter = java.text.NumberFormat.getCurrencyInstance(Locale("vi", "VN")).apply {
                currency = java.util.Currency.getInstance("VND")
                maximumFractionDigits = 0
            }
            val formattedMoney = formatter.format(discount).replace("₫", "").trim()
            "$formattedMoney VND OFF"
        }
        else -> "${discountFormat.format(discount)} OFF"
    }
}

private fun mapScope(scope: String?): String {
    return when (scope) {
        "Shipping" -> "SHIPPING"
        "Product" -> "PRODUCTS"
        else -> "STOREWIDE"
    }
}

private fun formatExpiryDate(value: String?): String {
    if (value.isNullOrBlank()) return "N/A"

    val cleanValue = value.substringBefore('.').removeSuffix("Z")
    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    val parsed = runCatching { inputFormat.parse(cleanValue) }.getOrNull() ?: return value
    val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
    return outputFormat.format(parsed).uppercase(Locale.US)
}