package com.example.shoestoreapp.features.invoice.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shoestoreapp.features.invoice.model.InvoiceStatus
import com.example.shoestoreapp.features.invoice.model.displayName

@Composable
fun StatusBadge(status: InvoiceStatus, modifier: Modifier = Modifier) {
    val (bg, fg) = when (status) {
        InvoiceStatus.PENDING -> Color(0xFFFFF8E1) to Color(0xFF9A6700)
        InvoiceStatus.PAID -> Color(0xFFE8F2FF) to Color(0xFF1F5FAE)
        InvoiceStatus.CANCELED -> Color(0xFFFFECEB) to Color(0xFFB3261E)
        InvoiceStatus.DELIVERING -> Color(0xFFEDEBFF) to Color(0xFF4C2A9B)
        InvoiceStatus.DELIVERED -> Color(0xFFEAF7EE) to Color(0xFF1E7D32)
    }

    Text(
        text = status.displayName(),
        modifier = modifier
            .background(color = bg, shape = RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        color = fg,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold
    )
}

