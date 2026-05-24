package com.example.shoestoreapp.features.admin.voucher.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.shoestoreapp.features.admin.product.ui.components.AdminBottomNavBar
import com.example.shoestoreapp.features.admin.product.ui.components.AdminBottomNavTab
import com.example.shoestoreapp.features.admin.voucher.ui.components.ActiveCampaignsList
import com.example.shoestoreapp.features.admin.voucher.ui.components.VoucherFormCard
import com.example.shoestoreapp.features.admin.voucher.ui.components.VoucherHeader
import com.example.shoestoreapp.features.admin.voucher.ui.components.VoucherTopAppBar
import com.example.shoestoreapp.features.admin.voucher.viewmodel.VoucherUiEvent
import com.example.shoestoreapp.features.admin.voucher.viewmodel.VoucherViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun VoucherManagementScreen(
    viewModel: VoucherViewModel = VoucherViewModel(),
    onTabSelected: (AdminBottomNavTab) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val vouchers by viewModel.vouchers.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is VoucherUiEvent.ShowSuccess -> {
                    android.widget.Toast.makeText(context, event.message, android.widget.Toast.LENGTH_SHORT).show()
                }
                is VoucherUiEvent.ShowError -> {
                    android.widget.Toast.makeText(context, event.message, android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = { VoucherTopAppBar() },
        bottomBar = {
            AdminBottomNavBar(
                selectedTab = AdminBottomNavTab.VOUCHERS,
                onTabSelected = onTabSelected
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                VoucherHeader()
            }
            item {
                VoucherFormCard(
                    uiState = uiState,
                    onVoucherNameChange = viewModel::updateVoucherName,
                    onDescriptionChange = viewModel::updateDescription,
                    onTargetApplicationChange = viewModel::updateTargetApplication,
                    onDiscountStyleChange = viewModel::updateDiscountStyle,
                    onDiscountValueChange = viewModel::updateDiscountValue,
                    onMaxReductionChange = viewModel::updateMaxReduction,
                    onMinOrderChange = viewModel::updateMinOrder,
                    onTotalQuantityChange = viewModel::updateTotalQuantity,
                    onMaxUsagePerUserChange = viewModel::updateMaxUsagePerUser,
                    onValidFromChange = viewModel::updateValidFrom,
                    onValidToChange = viewModel::updateValidTo,
                    onInitializeCampaign = viewModel::onCreateVoucherClick
                )
            }
            item {
                ActiveCampaignsList(vouchers = vouchers)
            }
        }
    }
}
