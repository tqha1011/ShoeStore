package com.example.shoestoreapp.features.admin.invoice.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shoestoreapp.features.invoice.model.InvoiceStatus
import com.example.shoestoreapp.features.invoice.ui.components.InvoiceStatusFilterChips

@Composable
fun AdminInvoiceFilterChips(
    selectedStatus: InvoiceStatus?,
    onFilterSelected: (InvoiceStatus?) -> Unit
) {
    InvoiceStatusFilterChips(
        selectedStatus = selectedStatus,
        onFilterSelected = onFilterSelected,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}
