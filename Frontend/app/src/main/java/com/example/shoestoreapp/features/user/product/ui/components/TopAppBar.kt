package com.example.shoestoreapp.features.user.product.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingBag
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
 * TopAppBar: Thanh ứng dụng phía trên cùng
 * @param onMenuClick - Callback khi click menu
 * @param onShoppingBagClick - Callback khi click shopping bag
 */
@Composable
fun TopAppBar(
    onMenuClick: () -> Unit = {},
    onShoppingBagClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Menu icon
        Icon(
            imageVector = Icons.Filled.Menu,
            contentDescription = "Menu",
            modifier = Modifier.clickable { onMenuClick() },
            tint = Color.Black
        )

        // Nike logo
        Text(
            text = "NIKE",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = Color.Black,
            letterSpacing = 2.sp
        )

        // Shopping bag icon
        Icon(
            imageVector = Icons.Filled.ShoppingBag,
            contentDescription = "Shopping Bag",
            modifier = Modifier.clickable { onShoppingBagClick() },
            tint = Color.Black
        )
    }
}

