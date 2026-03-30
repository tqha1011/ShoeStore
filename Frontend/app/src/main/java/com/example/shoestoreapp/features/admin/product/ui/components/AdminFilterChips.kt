package com.example.shoestoreapp.features.admin.product.ui.components
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
/**
 * Component hiển thị danh sách filter chips.
 * 
 * Tính năng:
 * - Hiển thị 4 filter chip: ALL, IN STOCK, LOW STOCK, OUT OF STOCK
 * - Chip được chọn có background đen, chữ trắng
 * - Chip chưa chọn có border xám, chữ xám
 * 
 * @param selectedFilter - Filter hiện tại được chọn
 * @param onFilterSelected - Callback khi user click vào filter
 */
@Composable
fun AdminFilterChips(
    filters: List<String> = listOf("ALL PRODUCTS", "IN STOCK", "LOW STOCK", "OUT OF STOCK"),
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { filter ->
            val isSelected = selectedFilter == filter
            Text(
                text = filter,
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
