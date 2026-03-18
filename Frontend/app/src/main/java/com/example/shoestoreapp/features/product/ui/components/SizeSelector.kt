package com.example.shoestoreapp.features.product.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * SizeSelector: Component cho phép user chọn kích thước giày
 *
 * Chức năng:
 * - Hiển thị grid kích thước (7, 8, 9, 10, 11)
 * - Hiển thị nút "Size Guide"
 * - Cho phép user click để chọn size
 * - Highlight size được chọn
 *
 * @param selectedSize - Size hiện tại được chọn (null nếu chưa chọn)
 * @param onSizeSelected - Callback khi user chọn size
 * @param onSizeGuideClick - Callback khi user click nút "Size Guide"
 * @param modifier - Modifier để tùy chỉnh layout
 */
@Composable
fun SizeSelector(
    modifier: Modifier = Modifier,
    selectedSize: Int?,
    onSizeSelected: (Int) -> Unit,
    onSizeGuideClick: () -> Unit = {}
) {
    // Danh sách các size có sẵn
    val availableSizes = listOf(7, 8, 9, 10, 11)

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Row 1: Tiêu đề "Select Size" + nút "Size Guide"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tiêu đề
            Text(
                text = "SELECT SIZE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                letterSpacing = 0.5.sp
            )

            // Nút "Size Guide"
            Row(
                modifier = Modifier
                    .clickable { onSizeGuideClick() }
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Straighten,
                    contentDescription = "Size Guide",
                    tint = Color.Gray,
                    modifier = Modifier.width(18.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "SIZE GUIDE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // Spacer: Tạo khoảng cách 16.dp
        Spacer(modifier = Modifier.height(16.dp))

        // Grid Size Buttons: 5 cột, mỗi hàng chứa 5 size button
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            availableSizes.forEach { size ->
                SizeButton(
                    size = size,
                    isSelected = selectedSize == size,
                    onClick = { onSizeSelected(size) },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                )
            }
        }
    }
}

/**
 * SizeButton: Component nút chọn size riêng lẻ
 *
 * @param size - Kích thước (7, 8, 9, 10, 11, v.v.)
 * @param isSelected - Có phải size đang được chọn không
 * @param onClick - Callback khi user click vào button
 * @param modifier - Modifier để tùy chỉnh layout
 */
@Composable
private fun SizeButton(
    size: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .border(
                width = 1.5.dp,
                color = if (isSelected) Color.Black else Color(0xFFE0E0E0),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .background(
                color = if (isSelected) Color.Black else Color.White
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = size.toString(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else Color.Black
        )
    }
}

