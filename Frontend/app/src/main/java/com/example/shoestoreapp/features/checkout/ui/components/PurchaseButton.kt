package com.example.shoestoreapp.features.checkout.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Component nút "Complete Purchase" (CTA chính).
 *
 * @param onCompletePurchaseClick - Callback khi người dùng click nút
 * @param isLoading - Có đang loading hay không
 * @param modifier - Modifier cho component
 *
 * UI:
 * - Full width button màu đen với text trắng
 * - Rounded corners (full)
 * - Active state: scale down
 */
@Composable
fun CompletePurchaseButton(
    modifier: Modifier = Modifier,
    onCompletePurchaseClick: () -> Unit,
    isLoading: Boolean = false,
) {
    Button(
        onClick = onCompletePurchaseClick,
        enabled = !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Black,
            contentColor = Color.White,
            disabledContainerColor = Color(0xFF999999)
        ),
        shape = RoundedCornerShape(28.dp)
    ) {
        Text(
            text = if (isLoading) "PROCESSING..." else "COMPLETE PURCHASE",
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.0.sp
        )
    }
}

/**
 * Component hiển thị thông báo disclaimer/terms.
 *
 * @param modifier - Modifier cho component
 */
@Composable
fun TermsDisclaimerText(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "By clicking \"Complete Purchase\", you agree to Nike's Terms of Use and Privacy Policy.",
            fontSize = 10.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF999999),
            lineHeight = 14.sp,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

