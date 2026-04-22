package com.example.shoestoreapp.features.admin.invoice.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shoestoreapp.features.admin.invoice.ui.components.AdminInvoiceFilterChips
import com.example.shoestoreapp.features.admin.invoice.viewmodel.AdminInvoiceViewModel
import com.example.shoestoreapp.features.admin.product.ui.components.AdminBottomNavBar
import com.example.shoestoreapp.features.admin.product.ui.components.AdminBottomNavTab
import com.example.shoestoreapp.features.invoice.model.displayName
import com.example.shoestoreapp.features.admin.invoice.ui.components.AdminOrderCard
import kotlinx.coroutines.launch

@Composable
fun AdminInvoiceScreen(
    viewModel: AdminInvoiceViewModel = AdminInvoiceViewModel(),
    onTabSelected: (AdminBottomNavTab) -> Unit = {}
) {
    val selectedStatus by viewModel.selectedStatus.collectAsState()
    val invoices by viewModel.visibleInvoices.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE8E8E8))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = Color.Black
                )
                Text(
                    text = "SHOE STORE",
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.6.sp
                )
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color(0xFF666666)
                )
            }
        },
        bottomBar = {
            AdminBottomNavBar(
                selectedTab = AdminBottomNavTab.ORDERS,
                onTabSelected = onTabSelected
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            Text(
                text = "Order Management",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                color = Color.Black
            )

            AdminInvoiceFilterChips(
                selectedStatus = selectedStatus,
                onFilterSelected = viewModel::onFilterChange
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(invoices) { invoice ->
                    AdminOrderCard(
                        invoice = invoice,
                        statusOptions = viewModel.getStatusOptions(invoice),
                        onStatusSelected = { targetStatus ->
                            viewModel.updateStatus(
                                orderCode = invoice.orderCode,
                                targetStatus = targetStatus
                            )

                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "Status updated to ${targetStatus.displayName()}",
                                    actionLabel = "Undo",
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    viewModel.undoLastStatusChange()
                                }
                            }
                        },
                        onDetailsClick = {}
                    )
                }
            }
        }
    }
}
