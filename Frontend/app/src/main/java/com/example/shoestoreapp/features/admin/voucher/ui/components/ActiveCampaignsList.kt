package com.example.shoestoreapp.features.admin.voucher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shoestoreapp.features.admin.voucher.data.remote.ResponseVoucherAdminDto
import java.text.NumberFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Currency
import java.util.Locale

@Composable
fun ActiveCampaignsList(
    vouchers: List<ResponseVoucherAdminDto>,
    onEditVoucherClick: (ResponseVoucherAdminDto) -> Unit,
    onDeleteClick: (String) -> Unit,
    onClearExpiredClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ListAlt,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Active Campaigns",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${vouchers.size} Running",
                    fontSize = 10.sp,
                    color = Color(0xFF6B6B6B),
                    modifier = Modifier
                        .background(Color(0xFFF2F2F2), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onClearExpiredClick) {
                    Icon(
                        imageVector = Icons.Default.CleaningServices,
                        contentDescription = "Clear expired vouchers",
                        tint = Color(0xFF6B6B6B)
                    )
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            vouchers.forEach { voucher ->
                VoucherItem(voucher, onEditVoucherClick, onDeleteClick)
            }
        }
    }
}

@Composable
private fun VoucherItem(
    voucher: ResponseVoucherAdminDto,
    onEditVoucherClick: (ResponseVoucherAdminDto) -> Unit,
    onDeleteClick: (String) -> Unit
) {
    val isPercentage = voucher.discountType?.equals("Percentage", ignoreCase = true) == true
    val isShipping = voucher.voucherScope?.equals("Shipping", ignoreCase = true) == true
    val isProduct = voucher.voucherScope?.equals("Product", ignoreCase = true) == true
    val scopeLabel = when {
        isShipping -> " Shipping"
        isProduct -> " Product"
        else -> "Product"
    }

    val summary = buildString {
        if (isPercentage) {
            append("${voucher.discount.toInt()}% off")
        } else {
            append("${formatMoney(voucher.discount)} off")
        }
        append(scopeLabel)
        if (isPercentage && voucher.maxPriceDiscount > 0.0) {
            append(" • Up to ${formatMoney(voucher.maxPriceDiscount)}")
        }
        if (voucher.minOrderPrice > 0.0) {
            append(" • Min ${formatMoney(voucher.minOrderPrice)}")
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E5E5))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFF6F6F6), CircleShape)
                        .border(1.dp, Color(0xFFEDEDED), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isPercentage) {
                        Text(
                            text = "%",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    } else if (isShipping) {
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = Color.Black
                        )
                    } else {
                        Text(
                            text = "₫",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = voucher.voucherName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = summary,
                        fontSize = 11.sp,
                        color = Color(0xFF6B6B6B)
                    )
                    Text(
                        text = "VALID UNTIL: ${formatDateDdMmYyyy(voucher.validTo)}",
                        fontSize = 10.sp,
                        color = Color(0xFF9A9A9A)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit voucher",
                            tint = Color(0xFF6B6B6B),
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { onEditVoucherClick(voucher) }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete voucher",
                            tint = Color(0xFF6B6B6B),
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { onDeleteClick(voucher.voucherGuid) }
                        )
                    }
                }
            }
        }
    }
}

private fun formatDateDdMmYyyy(raw: String): String {
    val input = raw.trim()
    if (input.isEmpty()) return ""

    val inputPatterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd"
    )
    val parsed = inputPatterns.firstNotNullOfOrNull { pattern ->
        val sdf = SimpleDateFormat(pattern, Locale.US).apply { isLenient = false }
        try {
            sdf.parse(input)
        } catch (_: ParseException) {
            null
        }
    } ?: return input

    val output = SimpleDateFormat("dd/MM/yyyy", Locale.US)
    return output.format(parsed)
}

private fun formatMoney(value: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN")).apply {
        currency = Currency.getInstance("VND")
        maximumFractionDigits = 0
    }
    return formatter.format(value)
}
