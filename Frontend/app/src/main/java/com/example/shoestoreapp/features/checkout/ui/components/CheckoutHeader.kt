package com.example.shoestoreapp.features.checkout.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Component header của Checkout Screen.
 *
 * Hiển thị:
 * - Title: "CHECKOUT" (chữ lớn, đậm)
 * - Subtitle: "Review your order and complete purchase." (chữ nhỏ hơn, xám)
 */
@Composable
fun CheckoutHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Text(
            text = "CHECKOUT",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp,
            color = Color.Black
        )

        Text(
            text = "Review your order and complete purchase.",
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF999999),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

