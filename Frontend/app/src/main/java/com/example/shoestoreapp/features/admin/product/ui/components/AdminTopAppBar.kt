package com.example.shoestoreapp.features.admin.product.ui.components
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
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
 * Top App Bar cho admin product management.
 * 
 * Cấu trúc:
 * - Menu icon bên trái
 * - NIKE text ở giữa
 * - Add Product button bên phải
 * 
 * @param onMenuClick - Callback khi click menu icon
 * @param onAddProductClick - Callback khi click Add Product button
 */
@Composable
fun AdminTopAppBar(
    onMenuClick: () -> Unit = {},
    onAddProductClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(Color.White)
            .border(width = 1.dp, color = Color(0xFFE8E8E8))
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Menu Icon
        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = "Menu",
            modifier = Modifier
                .clickable { onMenuClick() }
                .padding(8.dp),
            tint = Color.Black
        )
        // NIKE Text
        Text(
            text = "NIKE",
            fontWeight = FontWeight.Black,
            fontSize = 24.sp,
            letterSpacing = 2.sp,
            color = Color.Black
        )
        // Add Product Button
        Text(
            text = "Add Product",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = Color.Black,
            modifier = Modifier
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(4.dp)
                )
                .border(
                    width = 1.dp,
                    color = Color.Black,
                    shape = RoundedCornerShape(4.dp)
                )
                .clickable { onAddProductClick() }
                .padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

