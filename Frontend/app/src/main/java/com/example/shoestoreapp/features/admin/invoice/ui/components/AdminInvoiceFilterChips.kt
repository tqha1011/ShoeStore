package com.example.shoestoreapp.features.admin.invoice.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shoestoreapp.features.invoice.model.InvoiceStatus

@Composable
fun AdminInvoiceFilterChips(
    selectedStatus: InvoiceStatus?,
    onFilterSelected: (InvoiceStatus?) -> Unit
) {
    val filters = listOf<InvoiceStatus?>(null, InvoiceStatus.PENDING, InvoiceStatus.PAID, InvoiceStatus.DELIVERING, InvoiceStatus.CANCELED)

    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { filter ->
            val isSelected = selectedStatus == filter
            val text = when (filter) {
                null -> "ALL"
                InvoiceStatus.PENDING -> "PENDING"
                InvoiceStatus.PAID -> "PAID"
                InvoiceStatus.DELIVERING -> "DELIVERING"
                InvoiceStatus.CANCELED -> "CANCELED"
            }

            Text(
                text = text,
                modifier = Modifier
                    .background(
                        color = if (isSelected) Color.Black else Color.White,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .border(
                        width = if (isSelected) 0.dp else 1.dp,
                        color = if (isSelected) Color.Black else Color(0xFFE0E0E0),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { onFilterSelected(filter) }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                color = if (isSelected) Color.White else Color(0xFF999999),
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 0.8.sp
            )
        }
    }
}

