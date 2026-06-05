package com.example.shoestoreapp.features.admin.voucher.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.example.shoestoreapp.features.admin.product.ui.components.AdminFormField

@Composable
fun VoucherDurationSection(
    validFrom: String,
    validTo: String,
    onValidFromChange: (String) -> Unit,
    onValidToChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Campaign Duration",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                AdminFormField(
                    label = "Valid From",
                    value = validFrom,
                    onValueChange = onValidFromChange,
                    placeholder = "YYYY-MM-DD"
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                AdminFormField(
                    label = "Valid To",
                    value = validTo,
                    onValueChange = onValidToChange,
                    placeholder = "YYYY-MM-DD"
                )
            }
        }
    }
}

