package com.example.shoestoreapp.features.user.product.ui.components

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
 * @param name - Tên sản phẩm
 * @param price - Giá sản phẩm
 * @param rating - Đánh giá sao
 * @param reviewCount - Số lượng reviews
 * @param productType - Loại sản phẩm
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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

            Text(
                text = "$${String.format(Locale.US, "%.0f", price)}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = productType,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Rating",
                tint = Color.Black,
                modifier = Modifier.width(20.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = String.format(Locale.US, "%.1f", rating),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "•",
                fontSize = 14.sp,
                color = Color.LightGray
            )
            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "$reviewCount reviews",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )
        }
    }
}

