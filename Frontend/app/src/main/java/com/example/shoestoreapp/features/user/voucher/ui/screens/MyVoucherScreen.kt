package com.example.shoestoreapp.features.user.voucher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shoestoreapp.features.user.voucher.data.models.VoucherUiModel // Import class này vào
import com.example.shoestoreapp.features.user.voucher.ui.components.MyVoucherCard
import com.example.shoestoreapp.features.user.voucher.ui.components.SectionHeader
import com.example.shoestoreapp.features.user.voucher.ui.components.VoucherTopBar
import com.example.shoestoreapp.features.user.voucher.viewmodel.MyVoucherViewModel

@Composable
fun MyVoucherScreen(
    modifier: Modifier = Modifier,
    viewModel: MyVoucherViewModel = viewModel(),
    isSelectionMode: Boolean = false,
    cartTotal: Double? = null,
    onBackClick: () -> Unit,
    onApplyVoucher: (VoucherUiModel) -> Unit = {},
    onShopNowClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.fetchMyVouchers(isLoadMore = false)
    }

    val shouldLoadMore by remember(uiState.vouchers.size, listState) {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= uiState.vouchers.size - 1 && uiState.vouchers.isNotEmpty()
        }
    }

    LaunchedEffect(shouldLoadMore, uiState.hasNextPage, uiState.isLoading, uiState.isLoadingMore) {
        if (shouldLoadMore && uiState.hasNextPage && !uiState.isLoading && !uiState.isLoadingMore) {
            viewModel.fetchMyVouchers(isLoadMore = true)
        }
    }

    Scaffold(
        topBar = {
            VoucherTopBar(
                title = "MY VOUCHERS",
                onBackClick = onBackClick
            )
        },
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
        } else if (!uiState.isLoading && uiState.vouchers.isEmpty()) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "YOUR WALLET IS EMPTY",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
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
                        title = "Your Vouchers",
                        count = uiState.vouchers.size
                    )
                }
                items(uiState.vouchers, key = { it.id }) { voucher ->
                    MyVoucherCard(
                        voucher = voucher,
                        isUsed = voucher.isUsed,
                        cartTotal = cartTotal,
                        onUseClick = { _ ->
                            if (isSelectionMode) {
                                onApplyVoucher(voucher)
                            } else {
                                onShopNowClick()
                            }
                        }
                    )
                }
                if (uiState.isLoadingMore) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
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
}