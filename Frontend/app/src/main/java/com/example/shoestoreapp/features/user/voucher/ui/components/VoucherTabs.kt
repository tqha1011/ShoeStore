package com.example.shoestoreapp.features.user.voucher.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.example.shoestoreapp.features.user.voucher.data.models.VoucherStatus

@Composable
fun VoucherTabs(
    selectedStatus: VoucherStatus,
    onSelected: (VoucherStatus) -> Unit
) {
    val tabs = listOf(
        VoucherStatus.AVAILABLE,
        VoucherStatus.USED,
        VoucherStatus.EXPIRED
    )

    TabRow(selectedTabIndex = tabs.indexOf(selectedStatus)) {
        tabs.forEach { status ->
            Tab(
                selected = status == selectedStatus,
                onClick = { onSelected(status) },
                text = {
                    Text(
                        text = status.name,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (status == selectedStatus) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
        }
    }
}

