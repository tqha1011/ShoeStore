package com.example.shoestoreapp.features.checkout.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shoestoreapp.features.checkout.data.models.PaymentMethod
import com.example.shoestoreapp.features.checkout.data.models.PaymentType

/**
 * Component hiển thị danh sách phương thức thanh toán.
 *
 * @param selectedPaymentMethod - Phương thức thanh toán được chọn
 * @param availablePaymentMethods - Danh sách các phương thức thanh toán có sẵn
 * @param onPaymentMethodSelected - Callback khi người dùng chọn phương thức
 *
 * UI Structure:
 * - Header: "PAYMENT METHOD"
 * - List của Payment Method Cards
 *   - Selected: black border (2dp), checkmark icon
 *   - Unselected: gray border, empty circle
 */
@Composable
fun PaymentMethodSection(
    selectedPaymentMethod: PaymentMethod,
    availablePaymentMethods: List<PaymentMethod>,
    onPaymentMethodSelected: (PaymentMethod) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Header
        Text(
            text = "PAYMENT METHOD",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.2.sp,
            color = Color(0xFF181C22),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Payment Methods List
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            availablePaymentMethods.forEach { paymentMethod ->
                PaymentMethodCard(
                    paymentMethod = paymentMethod,
                    isSelected = paymentMethod.id == selectedPaymentMethod.id,
                    onSelect = { onPaymentMethodSelected(paymentMethod) }
                )
            }
        }
    }
}

/**
 * Component hiển thị một phương thức thanh toán đơn lẻ.
 *
 * @param paymentMethod - Thông tin phương thức thanh toán
 * @param isSelected - Có phải được chọn hay không
 * @param onSelect - Callback khi click
 */
@Composable
fun PaymentMethodCard(
    paymentMethod: PaymentMethod,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color.Black else Color(0xFFE0E0E0),
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                color = Color.White,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onSelect() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        PaymentMethodDetails(
            paymentMethod = paymentMethod,
            modifier = Modifier.weight(1f)
        )

        SelectionIndicator(isSelected = isSelected)
    }
}

@Composable
private fun PaymentMethodDetails(
    paymentMethod: PaymentMethod,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PaymentIconBadge(paymentType = paymentMethod.type)

        PaymentInfo(
            displayName = paymentMethod.displayName,
            expiryDate = paymentMethod.expiryDate
        )
    }
}

@Composable
private fun PaymentIconBadge(
    paymentType: PaymentType,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp, 24.dp)
            .background(
                color = if (paymentType == PaymentType.APPLE_PAY) Color.Black else Color(0xFFF5F5F5),
                shape = RoundedCornerShape(4.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        val (text, fontSize, fontWeight, color) = getPaymentBadgeStyle(paymentType)
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = fontWeight,
            color = color
        )
    }
}

private fun getPaymentBadgeStyle(
    paymentType: PaymentType
): Tuple4<String, androidx.compose.ui.unit.TextUnit, FontWeight, Color> {
    return if (paymentType == PaymentType.APPLE_PAY) {
        Tuple4("iOS", 14.sp, FontWeight.Bold, Color.White)
    } else {
        Tuple4(paymentType.name.take(4), 10.sp, FontWeight.Black, Color.Black)
    }
}

private data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
private fun PaymentInfo(
    displayName: String,
    expiryDate: String
) {
    Column {
        Text(
            text = displayName,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        if (expiryDate.isNotEmpty()) {
            Text(
                text = "Expires $expiryDate",
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF999999),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun SelectionIndicator(isSelected: Boolean) {
    if (isSelected) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Selected",
            modifier = Modifier.size(24.dp),
            tint = Color.Black
        )
    } else {
        Box(
            modifier = Modifier
                .size(20.dp)
                .border(
                    width = 1.dp,
                    color = Color(0xFFD0D0D0),
                    shape = RoundedCornerShape(50)
                )
        )
    }
}

