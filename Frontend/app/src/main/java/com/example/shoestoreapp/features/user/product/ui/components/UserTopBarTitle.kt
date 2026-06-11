package com.example.shoestoreapp.features.user.product.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun UserTopBarTitle(text: String) {
    Text(
        text = text,
        style = TextStyle(
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp,
            shadow = Shadow(
                color = Color.Gray.copy(alpha = 0.4f),
                offset = Offset(3f, 3f),
                blurRadius = 5f
            )
        ),
        color = Color.Black
    )
}
