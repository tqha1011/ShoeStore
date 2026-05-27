package com.example.shoestoreapp.features.admin.voucher.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.shoestoreapp.features.admin.product.ui.components.AdminFormField

@Composable
fun VoucherBasicInfoSection(
    voucherName: String,
    description: String,
    onVoucherNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AdminFormField(
            label = "Voucher Name",
            value = voucherName,
            onValueChange = onVoucherNameChange,
            placeholder = "e.g., Summer Sale 2024"
        )
        AdminFormField(
            label = "Description",
            value = description,
            onValueChange = onDescriptionChange,
            placeholder = "e.g., 20% off all orders above 100,000 ₫...",
            singleLine = false,
            minLines = 3
        )
    }
}
