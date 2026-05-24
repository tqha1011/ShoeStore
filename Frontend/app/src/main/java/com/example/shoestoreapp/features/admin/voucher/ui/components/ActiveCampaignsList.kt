package com.example.shoestoreapp.features.admin.voucher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shoestoreapp.features.admin.voucher.data.remote.ResponseVoucherAdminDto
import java.util.Locale

@Composable
fun ActiveCampaignsList(vouchers: List<ResponseVoucherAdminDto>) {
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
            Text(
                text = "${vouchers.size} Running",
                fontSize = 10.sp,
                color = Color(0xFF6B6B6B),
                modifier = Modifier
                    .background(Color(0xFFF2F2F2), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            vouchers.forEach { voucher ->
                VoucherItem(voucher)
            }
        }
    }
}

@Composable
private fun VoucherItem(voucher: ResponseVoucherAdminDto) {
    val summary = buildString {
        if (voucher.discountType == 0) {
            append("${voucher.discount.toInt()}% off")
        } else {
            append("${formatMoney(voucher.discount)} off")
        }
        append(if (voucher.voucherScope == 1) " Shipping" else " Invoice")
        if (voucher.discountType == 0 && voucher.maxPriceDiscount > 0.0) {
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
                    if (voucher.discountType == 0) {
                        Text(
                            text = "%",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    } else if (voucher.voucherScope == 1) {
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = Color.Black
                        )
                    } else {
                        Text(
                            text = "$",
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
                        text = "VALID UNTIL: ${voucher.validTo}",
                        fontSize = 10.sp,
                        color = Color(0xFF9A9A9A)
                    )
                }
            }
        }
    }
}

private fun formatMoney(value: Double): String {
    return "$" + String.format(Locale.US, "%.0f", value)
}

