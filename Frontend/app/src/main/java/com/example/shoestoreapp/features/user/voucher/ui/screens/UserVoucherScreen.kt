package com.example.shoestoreapp.features.user.voucher.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shoestoreapp.features.user.voucher.data.models.VoucherStatus
import com.example.shoestoreapp.features.user.voucher.viewmodel.UserVoucherViewModel
import com.example.shoestoreapp.features.user.voucher.ui.components.VoucherCard
import com.example.shoestoreapp.features.user.voucher.ui.components.VoucherTabs

@Composable
fun UserVoucherScreen(
    viewModel: UserVoucherViewModel = viewModel()
) {
    var selectedStatus by remember { mutableStateOf(VoucherStatus.AVAILABLE) }
    val uiState by viewModel.uiState.collectAsState()
    val filteredVouchers = remember(uiState.vouchers, selectedStatus) {
        uiState.vouchers.filter { it.status == selectedStatus }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "My Vouchers",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        VoucherTabs(selectedStatus = selectedStatus, onSelected = { selectedStatus = it })
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(filteredVouchers, key = { it.id }) { voucher ->
                VoucherCard(voucher = voucher)
            }
        }
    }
}
