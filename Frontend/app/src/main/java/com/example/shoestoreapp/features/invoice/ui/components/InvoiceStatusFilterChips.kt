package com.example.shoestoreapp.features.invoice.ui.components

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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shoestoreapp.features.invoice.model.InvoiceStatus

private val defaultFilters = listOf(
    null,
    InvoiceStatus.PENDING,
    InvoiceStatus.PAID,
    InvoiceStatus.DELIVERING,
    InvoiceStatus.DELIVERED,
    InvoiceStatus.CANCELED
)

@Composable
fun InvoiceStatusFilterChips(
    selectedStatus: InvoiceStatus?,
    onFilterSelected: (InvoiceStatus?) -> Unit,
    modifier: Modifier = Modifier,
    chipCornerRadius: Dp = 20.dp,
    chipHorizontalPadding: Dp = 16.dp,
    chipVerticalPadding: Dp = 10.dp,
    selectedColor: Color = Color.Black,
    selectedTextColor: Color = Color.White,
    unselectedColor: Color = Color.White,
    unselectedTextColor: Color = Color(0xFF999999),
    unselectedBorderColor: Color = Color(0xFFE0E0E0),
    showSelectedBorder: Boolean = false,
    fontSize: TextUnit = 10.sp,
    letterSpacing: TextUnit = 0.8.sp
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        defaultFilters.forEach { filter ->
            val isSelected = selectedStatus == filter
            Text(
                text = filter?.name ?: "ALL",
                modifier = Modifier
                    .background(
                        color = if (isSelected) selectedColor else unselectedColor,
                        shape = RoundedCornerShape(chipCornerRadius)
                    )
                    .border(
                        width = if (isSelected && !showSelectedBorder) 0.dp else 1.dp,
                        color = if (isSelected) selectedColor else unselectedBorderColor,
                        shape = RoundedCornerShape(chipCornerRadius)
                    )
                    .clickable { onFilterSelected(filter) }
                    .padding(horizontal = chipHorizontalPadding, vertical = chipVerticalPadding),
                color = if (isSelected) selectedTextColor else unselectedTextColor,
                fontWeight = FontWeight.Bold,
                fontSize = fontSize,
                letterSpacing = letterSpacing
            )
        }
    }
}

