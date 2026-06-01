package com.example.shoestoreapp.features.user.voucher.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shoestoreapp.features.user.voucher.data.models.VoucherUiModel
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

@Composable
fun MyVoucherCard(
    modifier: Modifier = Modifier,
    voucher: VoucherUiModel,
    isUsed: Boolean,
    cartTotal: Double? = null, // Dung nullable de tai su dung cho ca man hinh Vi (khong co gio hang)
    onUseClick: (String) -> Unit
) {
    // 1. Kiem tra dieu kien: Neu cartTotal = null (dang o man Vi) hoac lon hon minOrder thi cho phep
    val isEligible = cartTotal == null || cartTotal >= voucher.minOrderPrice

    // 2. Nut chi duoc bam khi chua dung va du dieu kien don hang
    val isClickable = !isUsed && isEligible

    // 3. Lam mo 50% neu da dung hoac khong du dieu kien
    val cardAlpha = if (isClickable) 1f else 0.5f

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .alpha(cardAlpha), // Ap dung do mo
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFF1F1F1)),
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Khoi hien thi muc giam gia
            Surface(
                color = Color(0xFFF9F9FF),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(96.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = voucher.discountValue.uppercase(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = voucher.scope.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9CA3AF),
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Khoi noi dung Text
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = voucher.title.uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = voucher.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF9CA3AF)
                )

                // Hien thi canh bao neu khong du dieu kien (Chi hien thi khi o man Checkout)
                if (!isEligible && cartTotal != null) {
                    val missingAmount = voucher.minOrderPrice - cartTotal
                    Text(
                        text = "Buy ${formatMissingMoney(missingAmount)} more to apply",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "EXPIRES ${voucher.expiryDate.uppercase()}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9CA3AF),
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { if (isClickable) onUseClick(voucher.id) },
                enabled = isClickable,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isClickable) Color.Black else Color(0xFFE5E7EB),
                    contentColor = if (isClickable) Color.White else Color(0xFF9CA3AF),
                    disabledContainerColor = Color(0xFFE5E7EB),
                    disabledContentColor = Color(0xFF9CA3AF)
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(6.dp))
            ) {
                Text(
                    text = when {
                        isUsed -> "USED"
                        !isEligible -> "NOT ELIGIBLE"
                        else -> "USE NOW"
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

// Ham ho tro format tien te rieng cho text canh bao
private fun formatMissingMoney(value: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN")).apply {
        currency = Currency.getInstance("VND")
        maximumFractionDigits = 0
    }
    return formatter.format(value).replace("₫", "VND")
}