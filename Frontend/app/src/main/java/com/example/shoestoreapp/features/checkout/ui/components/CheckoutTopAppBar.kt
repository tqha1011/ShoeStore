package com.example.shoestoreapp.features.checkout.ui.components

import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.automirrored.filled.ArrowBack

/**
 * Component TopAppBar cho Checkout Screen.
 *
 * Hiển thị:
 * - Title: "NIKE" ở giữa
 * - Navigation icon: Back button bên trái
 * - Action icon: Shopping Bag button bên phải
 *
 * @param onBackClick - Callback khi click back icon
 * @param onShoppingBagClick - Callback khi click shopping bag icon
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutTopAppBar(
    onBackClick: () -> Unit = {},
    onShoppingBagClick: () -> Unit = {}
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "NIKE",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp,
                color = Color.Black
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }
        },
        actions = {
            IconButton(onClick = onShoppingBagClick) {
                Icon(
                    imageVector = Icons.Default.ShoppingBag,
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

