package com.example.shoestoreapp.features.user.voucher.data.remote

import com.example.shoestoreapp.features.user.voucher.data.models.VoucherDiscountType
import com.example.shoestoreapp.features.user.voucher.data.models.VoucherStatus
import com.example.shoestoreapp.features.user.voucher.data.models.VoucherUiModel
import java.text.SimpleDateFormat
import java.text.NumberFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToLong

private val uiDateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.US)

fun ResponseVoucherUserDto.toUiModel(nowDate: Date = Date()): VoucherUiModel {
    val parsedValidTo = parseApiDate(validTo)
    val status = when {
        isUsed -> VoucherStatus.USED
        parsedValidTo != null && nowDate.after(parsedValidTo) -> VoucherStatus.EXPIRED
        else -> VoucherStatus.AVAILABLE
    }

    val scopeSubtitle = if (voucherScope == 2) {
        "SHIPPING"
    } else {
        if (minOrderPrice > 0) {
            "MIN. SPEND ${formatVnd(minOrderPrice)}"
        } else {
            "STOREWIDE"
        }
    }

    val (discountValue, discountType) = when (voucherScope) {
        2 -> "FREE" to VoucherDiscountType.FREESHIP
        else -> when (this.discountType) {
            2 -> "${discount.toInt()}% OFF" to VoucherDiscountType.PERCENTAGE
            1 -> "${formatVnd(discount)} OFF" to VoucherDiscountType.FIXED
            else -> "${formatVnd(discount)} OFF" to VoucherDiscountType.FIXED
        }
    }

    val expiryDate = parsedValidTo?.let {
        uiDateFormatter.format(it)
    } ?: ""

    return VoucherUiModel(
        id = voucherGuid.ifBlank { voucherId.toString() },
        title = voucherName.orEmpty(),
        description = description.orEmpty(),
        expiryDate = expiryDate,
        discountValue = discountValue,
        discountType = discountType,
        scopeSubtitle = scopeSubtitle,
        status = status
    )
}

// Hàm parse ngày tháng "đồ cổ" chuyên trị chuỗi ISO từ Backend
private fun parseApiDate(value: String?): Date? {
    if (value.isNullOrBlank()) return null
    return try {
        // Cắt bỏ phần đuôi mili-giây và chữ Z (ví dụ: 2026-05-24T12:26:38.741Z -> 2026-05-24T12:26:38)
        // để SimpleDateFormat dễ thở hơn
        val cleanValue = value.substringBefore('.').removeSuffix("Z")
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        formatter.parse(cleanValue)
    } catch (_: Exception) {
        null
    }
}

private fun formatVnd(price: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    return "${formatter.format(price.roundToLong())} ₫"
}
