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
    InvoiceStatus.CANCELLED
)

data class InvoiceStatusFilterChipDimensions(
    val chipCornerRadius: Dp = 20.dp,
    val chipHorizontalPadding: Dp = 16.dp,
    val chipVerticalPadding: Dp = 10.dp
)

data class InvoiceStatusFilterChipColors(
    val selectedColor: Color = Color.Black,
    val selectedTextColor: Color = Color.White,
    val unselectedColor: Color = Color.White,
    val unselectedTextColor: Color = Color(0xFF999999),
    val unselectedBorderColor: Color = Color(0xFFE0E0E0)
)

data class InvoiceStatusFilterChipTypography(
    val fontSize: TextUnit = 10.sp,
    val letterSpacing: TextUnit = 0.8.sp
)

data class InvoiceStatusFilterChipStyle(
    val dimensions: InvoiceStatusFilterChipDimensions = InvoiceStatusFilterChipDimensions(),
    val colors: InvoiceStatusFilterChipColors = InvoiceStatusFilterChipColors(),
    val typography: InvoiceStatusFilterChipTypography = InvoiceStatusFilterChipTypography(),
    val showSelectedBorder: Boolean = false
)

@Composable
fun InvoiceStatusFilterChips(
    selectedStatus: InvoiceStatus?,
    onFilterSelected: (InvoiceStatus?) -> Unit,
    modifier: Modifier = Modifier,
    style: InvoiceStatusFilterChipStyle = InvoiceStatusFilterChipStyle(),
    filters: List<InvoiceStatus?> = defaultFilters
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { filter ->
            val isSelected = selectedStatus == filter
            Text(
                text = filter?.name ?: "ALL",
                modifier = Modifier
                    .background(
                        color = if (isSelected) style.colors.selectedColor else style.colors.unselectedColor,
                        shape = RoundedCornerShape(style.dimensions.chipCornerRadius)
                    )
                    .border(
                        width = if (isSelected && !style.showSelectedBorder) 0.dp else 1.dp,
                        color = if (isSelected) style.colors.selectedColor else style.colors.unselectedBorderColor,
                        shape = RoundedCornerShape(style.dimensions.chipCornerRadius)
                    )
                    .clickable { onFilterSelected(filter) }
                    .padding(
                        horizontal = style.dimensions.chipHorizontalPadding,
                        vertical = style.dimensions.chipVerticalPadding
                    ),
                color = if (isSelected) style.colors.selectedTextColor else style.colors.unselectedTextColor,
                fontWeight = FontWeight.Bold,
                fontSize = style.typography.fontSize,
                letterSpacing = style.typography.letterSpacing
            )
        }
    }
}

