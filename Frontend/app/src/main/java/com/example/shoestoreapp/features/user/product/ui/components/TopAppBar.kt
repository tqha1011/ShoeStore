package com.example.shoestoreapp.features.user.product.ui.components

import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.filled.AutoAwesome

/**
 * TopAppBar: Thanh ứng dụng phía trên cùng (sử dụng CenterAlignedTopAppBar)
 * @param onAiClick - Callback when click Ai Assistant
 * @param onShoppingBagClick - Callback khi click shopping bag
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    onAiClick: () -> Unit = {},
    onShoppingBagClick: () -> Unit = {}
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "SHOESTORE",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color.Black,
                letterSpacing = 2.sp
            )
        },
        navigationIcon = {
            IconButton(onClick = onAiClick) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AiAssistant",
                    tint = Color.Black
                )
            }
        },
        actions = {
            IconButton(onClick = onShoppingBagClick) {
                Icon(
                    imageVector = Icons.Filled.ShoppingBag,
                    contentDescription = "Shopping Bag",
                    tint = Color.Black
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.White,
            titleContentColor = Color.Black,
            navigationIconContentColor = Color.Black,
            actionIconContentColor = Color.Black
        ),
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(top = 12.dp)
    )
}

