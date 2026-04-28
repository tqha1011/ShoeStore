package com.example.shoestoreapp.features.user.invoice.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shoestoreapp.features.invoice.model.Invoice
import com.example.shoestoreapp.features.invoice.model.InvoiceStatus
import com.example.shoestoreapp.features.invoice.ui.components.InvoiceStatusFilterChipColors
import com.example.shoestoreapp.features.invoice.ui.components.InvoiceStatusFilterChipDimensions
import com.example.shoestoreapp.features.invoice.ui.components.InvoiceStatusFilterChipStyle
import com.example.shoestoreapp.features.invoice.ui.components.InvoiceStatusFilterChipTypography
import com.example.shoestoreapp.features.invoice.ui.components.InvoiceStatusFilterChips
import com.example.shoestoreapp.features.user.invoice.ui.components.UserInvoiceDetailsBottomSheet
import com.example.shoestoreapp.features.user.invoice.ui.components.UserOrderCard
import com.example.shoestoreapp.features.user.invoice.viewmodel.UserInvoiceViewModel
import com.example.shoestoreapp.features.user.product.ui.components.BottomNavBar
import com.example.shoestoreapp.features.user.product.ui.components.BottomNavTab

@Composable
fun UserInvoiceScreen(
    viewModel: UserInvoiceViewModel,
    initialStatus: InvoiceStatus? = null,
    onTabSelected: (BottomNavTab) -> Unit = {}
) {
    val state = viewModel.state
    // Hosts transient success/error messages.
    val snackbarHostState = remember { SnackbarHostState() }
    // Holds the card selected for cancel confirmation.
    var invoiceToConfirmCancel by remember { mutableStateOf<Invoice?>(null) }

    LaunchedEffect(initialStatus) {
        // Apply route filter when screen is first opened.
        viewModel.applyInitialFilter(initialStatus)
    }

    LaunchedEffect(state.error, state.successMessage) {
        // Consume one-time messages from ViewModel state.
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearTransientMessage()
        }
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearTransientMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            BottomNavBar(
                selectedTab = BottomNavTab.BAG,
                onTabSelected = onTabSelected
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "My Orders",
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )

            UserInvoiceFilterRow(
                selectedStatus = state.selectedStatus,
                onFilterSelected = viewModel::onFilterSelected
            )

            when {
                state.isLoading -> {
                    Text(
                        text = "Loading orders...",
                        color = Color(0xFF666666),
                        fontSize = 14.sp
                    )
                }

                state.error != null && state.invoices.isEmpty() -> {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = state.error,
                            color = Color(0xFFB3261E),
                            fontSize = 14.sp
                        )
                        Button(
                            onClick = viewModel::loadInvoices,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Black,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Retry")
                        }
                    }
                }

                viewModel.filteredInvoices.isEmpty() -> {
                    Text(
                        text = "No orders found for this status.",
                        color = Color(0xFF666666),
                        fontSize = 14.sp
                    )
                }

                else -> {
                    // Main list with per-item actions.
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(
                            items = viewModel.filteredInvoices,
                            key = { invoice -> invoice.publicId }
                        ) { invoice ->
                            UserOrderCard(
                                invoice = invoice,
                                isCancelling = state.isCancelling && state.cancellingInvoicePublicId == invoice.publicId,
                                onCancelClick = {
                                    // Show confirm before firing cancel API.
                                    invoiceToConfirmCancel = invoice
                                },
                                onDetailsClick = {
                                    viewModel.openInvoiceDetails(invoice)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (state.selectedInvoice != null) {
        // Details sheet is driven by selectedInvoice presence.
        UserInvoiceDetailsBottomSheet(
            state = state,
            onDismissRequest = viewModel::clearDetails
        )
    }

    invoiceToConfirmCancel?.let { targetInvoice ->
        // Confirm dialog for list-level cancel action.
        AlertDialog(
            onDismissRequest = { invoiceToConfirmCancel = null },
            title = { Text(text = "Confirm cancellation") },
            text = { Text(text = "Are you sure you want to cancel this order?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        invoiceToConfirmCancel = null
                        viewModel.cancelInvoice(targetInvoice)
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { invoiceToConfirmCancel = null }) {
                    Text("Keep order")
                }
            }
        )
    }
}

@Composable
private fun UserInvoiceFilterRow(
    selectedStatus: InvoiceStatus?,
    onFilterSelected: (InvoiceStatus?) -> Unit
) {
    InvoiceStatusFilterChips(
        selectedStatus = selectedStatus,
        onFilterSelected = onFilterSelected,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 2.dp),
        style = InvoiceStatusFilterChipStyle(
            dimensions = InvoiceStatusFilterChipDimensions(
                chipCornerRadius = 18.dp,
                chipHorizontalPadding = 12.dp,
                chipVerticalPadding = 7.dp
            ),
            colors = InvoiceStatusFilterChipColors(
                unselectedTextColor = Color(0xFF888888)
            ),
            typography = InvoiceStatusFilterChipTypography(letterSpacing = 0.sp),
            showSelectedBorder = true
        )
    )
}
