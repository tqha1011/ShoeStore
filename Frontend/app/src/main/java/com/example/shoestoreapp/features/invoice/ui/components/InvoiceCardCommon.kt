package com.example.shoestoreapp.features.invoice.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shoestoreapp.features.invoice.model.InvoiceStatus

@Composable
fun InvoiceCardContainer(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFFE5E5E5)),
        content = content
    )
}

fun invoiceTextOrDash(value: String?): String {
    return value?.trim().orEmpty().ifEmpty { "-" }
}

@Composable
fun InvoiceStatusOrUnknown(
    status: InvoiceStatus?,
    unknownFontSize: TextUnit = 12.sp
) {
    status?.let {
        StatusBadge(status = it)
    } ?: Text(
        text = "Unknown",
        color = Color(0xFF8C8C8C),
        fontSize = unknownFontSize,
        fontWeight = FontWeight.Medium
    )
}

