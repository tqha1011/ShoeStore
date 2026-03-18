package com.example.shoestoreapp.features.product.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

/**
 * ProductHeaderInfo: Component hiển thị thông tin cơ bản sản phẩm (tên, giá, đánh giá)
 *
 * Chức năng:
 * - Hiển thị tên sản phẩm
 * - Hiển thị giá sản phẩm
 * - Hiển thị đánh giá (số sao)
 * - Hiển thị số lượng reviews
 *
 * @param name - Tên sản phẩm
 * @param price - Giá sản phẩm
 * @param rating - Đánh giá sao (ví dụ: 4.8)
 * @param reviewCount - Số lượng reviews
 * @param productType - Loại sản phẩm (ví dụ: "Men's Shoes")
 * @param modifier - Modifier để tùy chỉnh layout
 */
@Composable
fun ProductHeaderInfo(
    modifier: Modifier = Modifier,
    name: String,
    price: Double,
    rating: Double,
    reviewCount: Int,
    productType: String = "Men's Shoes"
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Row 1: Tên sản phẩm + Giá
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tên sản phẩm
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = name,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Black
                )
            }

            // Giá sản phẩm
            Text(
                text = "$${String.format(Locale.US, "%.0f", price)}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Color.Black
            )
        }

        // Spacer: Tạo khoảng cách 8dp
        Spacer(modifier = Modifier.height(8.dp))

        // Row 2: Loại sản phẩm
        Text(
            text = productType,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )

        // Spacer: Tạo khoảng cách 16.dp
        Spacer(modifier = Modifier.height(16.dp))

        // Row 3: Đánh giá + số reviews
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Icon sao
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Rating",
                tint = Color.Black,
                modifier = Modifier.width(20.dp)
            )

            // Spacer: Tạo khoảng cách 8dp
            Spacer(modifier = Modifier.width(8.dp))

            // Số đánh giá
            Text(
                text = String.format(Locale.US, "%.1f", rating),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            // Dấu chấm phân cách
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "•",
                fontSize = 14.sp,
                color = Color.LightGray
            )
            Spacer(modifier = Modifier.width(8.dp))

            // Số reviews dưới dạng link
            Text(
                text = "$reviewCount reviews",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )
        }
    }
}
