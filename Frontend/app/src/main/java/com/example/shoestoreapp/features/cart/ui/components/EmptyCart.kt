package com.example.shoestoreapp.features.cart.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * EmptyCart Component
 * Hiển thị khi giỏ hàng trống
 *
 * @param onContinueShopping - Callback khi click button tiếp tục mua sắm
 */
@Composable
fun EmptyCart(
    onContinueShopping: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Your shopping bag is empty",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Text(
            text = "Add items to get started",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp)
        )

        androidx.compose.material3.Button(
            onClick = onContinueShopping,
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Text("Continue Shopping")
        }
    }
}

