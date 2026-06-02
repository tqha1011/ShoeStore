package com.example.shoestoreapp.features.cart.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shoestoreapp.features.cart.data.models.CartSummary
import java.util.Locale

/**
 * CartSummary Component
 * Hiển thị tóm tắt giá (subtotal, shipping, tax, total)
 *
 * @param summary - CartSummary object
 */
@Composable
fun CartSummarySection(
    summary: CartSummary
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text(
            text = "Summary",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Subtotal
        SummaryRow(
            label = "Subtotal",
            value = "$${String.format(Locale.US,"%.2f", summary.subtotal)}",
            isHighlight = false
        )

        // Shipping
        SummaryRow(
            label = "Estimated Shipping & Handling",
            value = "$${String.format(Locale.US,"%.2f", summary.shippingCost)}",
            isHighlight = false
        )

        // Tax
        SummaryRow(
            label = "Estimated Tax",
            value = if (summary.tax == 0.0) "—" else "$${String.format(Locale.US,"%.2f", summary.tax)}",
            isHighlight = false
        )

        // Divider
        androidx.compose.material3.HorizontalDivider(
            modifier = Modifier.padding(vertical = 12.dp),
            color = Color(0xFFE0E0E0)
        )

        // Total
        SummaryRow(
            label = "Total",
            value = "$${String.format(Locale.US,"%.2f", summary.getTotal())}",
            isHighlight = true
        )
    }
}

/**
 * SummaryRow Component
 * Component con để hiển thị một dòng trong summary
 */
@Composable
private fun SummaryRow(
    label: String,
    value: String,
    isHighlight: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = if (isHighlight) 14.sp else 12.sp,
            color = Color.Gray,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value,
            fontSize = if (isHighlight) 16.sp else 12.sp,
            fontWeight = if (isHighlight) FontWeight.Black else FontWeight.Medium,
            color = Color.Black
        )
    }
}

