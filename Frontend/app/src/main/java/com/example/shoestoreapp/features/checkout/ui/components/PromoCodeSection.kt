package com.example.shoestoreapp.features.checkout.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Component nhập và áp dụng mã khuyến mãi (Promo Code).
 *
 * @param onApplyPromoCode - Callback khi người dùng click nút "Apply"
 * @param modifier - Modifier cho component
 *
 * UI Structure:
 * - Header: "PROMO CODE"
 * - Input field + Apply button
 * - Input placeholder: "ENTER CODE"
 */
@Composable
fun PromoCodeSection(
    onApplyPromoCode: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val promoCodeInput = remember { mutableStateOf("") }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Header
        Text(
            text = "PROMO CODE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.2.sp,
            color = Color(0xFF181C22),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Input + Button Row
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Text Input
            OutlinedTextField(
                value = promoCodeInput.value,
                onValueChange = { promoCodeInput.value = it.uppercase() },
                placeholder = {
                    Text(
                        text = "ENTER CODE",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD0D0D0),
                        letterSpacing = 0.8.sp
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(8.dp)
                    ),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.Black,
                    unfocusedIndicatorColor = Color(0xFFE0E0E0),
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                ),
                textStyle = androidx.compose.material3.LocalTextStyle.current.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            )

            // Apply Button
            Button(
                onClick = {
                    onApplyPromoCode(promoCodeInput.value)
                    promoCodeInput.value = ""
                },
                modifier = Modifier
                    .background(
                        color = Color.Black,
                        shape = RoundedCornerShape(8.dp)
                    ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "APPLY",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.8.sp,
                    color = Color.White
                )
            }
        }
    }
}

