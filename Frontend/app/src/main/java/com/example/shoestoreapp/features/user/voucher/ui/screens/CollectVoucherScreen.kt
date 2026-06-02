package com.example.shoestoreapp.features.user.voucher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shoestoreapp.features.user.product.ui.components.BottomNavBar
import com.example.shoestoreapp.features.user.product.ui.components.BottomNavTab
import com.example.shoestoreapp.features.user.product.ui.components.TopBanner
import com.example.shoestoreapp.features.user.voucher.ui.components.CollectVoucherCard
import com.example.shoestoreapp.features.user.voucher.ui.components.VoucherTopBar
import com.example.shoestoreapp.features.user.voucher.viewmodel.CollectVoucherViewModel
import com.example.shoestoreapp.features.user.voucher.ui.components.SectionHeader
import androidx.compose.runtime.collectAsState

@Composable
fun CollectVoucherScreen(
    modifier: Modifier = Modifier,
    viewModel: CollectVoucherViewModel = viewModel(),
    onTabSelected: (BottomNavTab) -> Unit = { }
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val shouldLoadMore by remember(uiState.vouchers.size, listState) {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= uiState.vouchers.size - 1 && uiState.vouchers.isNotEmpty()
        }
    }

    LaunchedEffect(shouldLoadMore, uiState.hasNextPage, uiState.isLoading, uiState.isLoadingMore) {
        if (shouldLoadMore && uiState.hasNextPage && !uiState.isLoading && !uiState.isLoadingMore) {
            viewModel.fetchVouchers(isLoadMore = true)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = { VoucherTopBar(
                title = "COLLECT VOUCHERS"
            ) },
            bottomBar = { BottomNavBar(
                selectedTab = BottomNavTab.VOUCHER,
                onTabSelected = onTabSelected
            )},
            containerColor = Color.White
        ) { innerPadding ->
            if (uiState.isLoading && uiState.vouchers.isEmpty()) {
                Box(
                    modifier = modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.Black)
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .padding(innerPadding)
                        .padding(horizontal = 24.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    item {
                        SectionHeader(
                            title = "Available Vouchers",
                            count = uiState.vouchers.size
                        )
                    }
                    items(uiState.vouchers, key = { it.id }) { voucher ->
                        CollectVoucherCard(
                            voucher = voucher,
                            onCollect = { voucherId, onSuccess, onError ->
                                viewModel.markCollected(voucherId, onSuccess, onError)
                            }
                        )
                    }
                    if (uiState.isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color.Black)
                            }
                        }
                    }
                }
            }
        }

        Box(modifier = Modifier.align(Alignment.TopCenter)) {
            TopBanner(
                message = uiState.bannerMessage,
                isSuccess = uiState.isBannerSuccess,
                isVisible = uiState.showBanner,
                onDismiss = { viewModel.hideBanner() }
            )
        }
    }
}
