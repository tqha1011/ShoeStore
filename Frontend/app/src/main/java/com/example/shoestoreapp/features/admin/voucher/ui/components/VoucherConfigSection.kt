package com.example.shoestoreapp.features.admin.voucher.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.example.shoestoreapp.features.admin.product.ui.components.AdminFormField
import com.example.shoestoreapp.features.admin.voucher.viewmodel.VoucherUiState

@Composable
fun VoucherConfigSection(
    uiState: VoucherUiState,
    onTargetApplicationChange: (Int) -> Unit,
    onDiscountStyleChange: (Int) -> Unit,
    onDiscountValueChange: (String) -> Unit,
    onMaxReductionChange: (String) -> Unit,
    onMinOrderChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Target Application",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioRowItem(
                label = "Total Invoice",
                selected = uiState.targetApplication == 0,
                onClick = { onTargetApplicationChange(0) }
            )
            RadioRowItem(
                label = "Shipping Fee",
                selected = uiState.targetApplication == 1,
                onClick = { onTargetApplicationChange(1) }
            )
        }

        Text(
            text = "Discount Style",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioRowItem(
                label = "Percentage (%)",
                selected = uiState.discountStyle == 0,
                onClick = { onDiscountStyleChange(0) }
            )
            RadioRowItem(
                label = "Fixed Amount ($)",
                selected = uiState.discountStyle == 1,
                onClick = { onDiscountStyleChange(1) }
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AdminFormField(
                label = if (uiState.discountStyle == 0) "Discount Value (%)" else "Discount Value ($)",
                value = uiState.discountValue,
                onValueChange = onDiscountValueChange,
                placeholder = if (uiState.discountStyle == 0) "0.00 (%)" else "0.00",
                keyboardType = KeyboardType.Number
            )
            if (uiState.discountStyle == 0) {
                AdminFormField(
                    label = "Max Reduction ($ Cap amount)",
                    value = uiState.maxReduction,
                    onValueChange = onMaxReductionChange,
                    placeholder = "Cap amount",
                    keyboardType = KeyboardType.Number
                )
            }
            AdminFormField(
                label = "Condition (Min Order)",
                value = uiState.minOrder,
                onValueChange = onMinOrderChange,
                placeholder = "Trigger value",
                keyboardType = KeyboardType.Number
            )
        }
    }
}
