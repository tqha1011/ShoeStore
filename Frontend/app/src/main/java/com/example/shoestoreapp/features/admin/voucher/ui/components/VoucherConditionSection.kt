package com.example.shoestoreapp.features.admin.voucher.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.shoestoreapp.features.admin.product.ui.components.AdminFormField

@Composable
fun VoucherConditionSection(
    totalQuantity: String,
    maxUsagePerUser: String,
    onTotalQuantityChange: (String) -> Unit,
    onMaxUsagePerUserChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                AdminFormField(
                    label = "Total Supply",
                    value = totalQuantity,
                    onValueChange = onTotalQuantityChange,
                    placeholder = "e.g., 1000",
                    keyboardType = KeyboardType.Number
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                AdminFormField(
                    label = "Usage Limit Per User",
                    value = maxUsagePerUser,
                    onValueChange = onMaxUsagePerUserChange,
                    placeholder = "e.g., 1",
                    keyboardType = KeyboardType.Number
                )
            }
        }
    }
}

