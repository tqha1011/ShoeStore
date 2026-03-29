package com.example.shoestoreapp.features.user.product.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * FilterChips: Danh sách các chip lọc sản phẩm
 * @param filters - Danh sách các bộ lọc (VD: "All Shoes", "Air Max", etc.)
 * @param selectedFilter - Bộ lọc hiện tại được chọn (state được lift từ parent)
 * @param onFilterSelected - Callback khi chọn một bộ lọc
 */
@Composable
fun FilterChips(
    filters: List<String> = listOf("All Shoes", "Air Max", "Dunk", "Pegasus", "Jordan"),
    selectedFilter: String = filters.first(),
    onFilterSelected: (String) -> Unit = {}
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 0.dp)
    ) {
        filters.forEach { filter ->
            val isSelected = filter == selectedFilter

            Box(
                modifier = Modifier
                    .background(
                        color = if (isSelected) Color.Black else Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(50.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable {
                        onFilterSelected(filter)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = filter,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else Color.Black,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

