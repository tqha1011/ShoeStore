package com.example.shoestoreapp.features.user.invoice.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shoestoreapp.core.utils.TokenManager
import com.example.shoestoreapp.features.invoice.model.Invoice
import com.example.shoestoreapp.features.invoice.model.InvoiceStatus
import com.example.shoestoreapp.features.user.invoice.data.OrderStatusSignalRManager
import com.example.shoestoreapp.features.user.invoice.ui.components.UserInvoiceDetailsBottomSheet
import com.example.shoestoreapp.features.user.invoice.ui.components.UserOrderCard
import com.example.shoestoreapp.features.user.invoice.viewmodel.UserInvoiceViewModel
import com.example.shoestoreapp.features.user.product.ui.components.BottomNavBar
import com.example.shoestoreapp.features.user.product.ui.components.BottomNavTab
import kotlinx.coroutines.launch

@Composable
fun UserInvoiceScreen(
    viewModel: UserInvoiceViewModel,
    initialStatus: InvoiceStatus? = null,
    onTabSelected: (BottomNavTab) -> Unit = {},
    onNavigateToPendingPayment: (Invoice) -> Unit = {}
) {
    val state = viewModel.state
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    // Hosts transient success/error messages.
    val snackbarHostState = remember { SnackbarHostState() }
    // Holds the card selected for cancel confirmation.
    var invoiceToConfirmCancel by remember { mutableStateOf<Invoice?>(null) }
    val tokenManager = remember { TokenManager(context) }
    val orderStatusSignalRManager = remember {
        OrderStatusSignalRManager(context, tokenManager)
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {}
    )

    LaunchedEffect(initialStatus) {
        // Apply route filter when screen is first opened.
        viewModel.applyInitialFilter(initialStatus)
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(orderStatusSignalRManager) {
        orderStatusSignalRManager.startConnection()
    }

    LaunchedEffect(orderStatusSignalRManager) {
        orderStatusSignalRManager.notifications.collect { notification ->
            viewModel.onOrderStatusNotification(notification)
        }
    }

    DisposableEffect(orderStatusSignalRManager) {
        onDispose {
            orderStatusSignalRManager.stopConnection()
        }
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
                selectedTab = BottomNavTab.PROFILE,
                onTabSelected = onTabSelected
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F7F8))
                .padding(paddingValues)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OrdersHeader()

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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = viewModel.filteredInvoices,
                            key = { invoice -> invoice.publicId }
                        ) { invoice ->
                            UserOrderCard(
                                invoice = invoice,
                                previewDetail = state.orderPreviewDetails[invoice.publicId],
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
            onDismissRequest = viewModel::clearDetails,
            onPayPendingSePayClick = { invoice ->
                if (invoice.hasQrPaymentData()) {
                    viewModel.clearDetails()
                    onNavigateToPendingPayment(invoice)
                } else {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Không đủ thông tin thanh toán để tạo mã QR.")
                    }
                }
            }
        )
    }

    invoiceToConfirmCancel?.let { targetInvoice ->
        // Confirm dialog for list-level cancel action.
        AlertDialog(
            onDismissRequest = { invoiceToConfirmCancel = null },
            containerColor = Color.White,
            titleContentColor = Color.Black,
            textContentColor = Color(0xFF4A4447),
            title = {
                Text(
                    text = "Confirm cancellation",
                    fontWeight = FontWeight.ExtraBold
                )
            },
            text = { Text(text = "Are you sure you want to cancel this order?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        invoiceToConfirmCancel = null
                        viewModel.cancelInvoice(targetInvoice)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Black)
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { invoiceToConfirmCancel = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF5D5659))
                ) {
                    Text("Keep order")
                }
            }
        )
    }
}

@Composable
private fun OrdersHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        IconButton(onClick = {}) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search orders",
                tint = Color.Black
            )
        }

        Text(
            text = "Orders",
            color = Color.Black,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black
        )

        IconButton(onClick = {}) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More order options",
                tint = Color.Black
            )
        }
    }
}

@Composable
private fun UserInvoiceFilterRow(
    selectedStatus: InvoiceStatus?,
    onFilterSelected: (InvoiceStatus?) -> Unit
) {
    val filters = listOf(
        OrderFilter(null, "All"),
        OrderFilter(InvoiceStatus.PENDING, "To confirm"),
        OrderFilter(InvoiceStatus.PAID, "To ship"),
        OrderFilter(InvoiceStatus.DELIVERING, "Shipping"),
        OrderFilter(InvoiceStatus.DELIVERED, "Completed"),
        OrderFilter(InvoiceStatus.CANCELLED, "Cancelled")
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        filters.forEach { filter ->
            val isSelected = selectedStatus == filter.status
            Text(
                text = filter.label,
                modifier = Modifier
                    .background(
                        color = if (isSelected) Color.Black else Color(0xFFF0F0F1),
                        shape = RoundedCornerShape(999.dp)
                    )
                    .clickable { onFilterSelected(filter.status) }
                    .padding(horizontal = 22.dp, vertical = 12.dp),
                color = if (isSelected) Color.White else Color(0xFF5D6570),
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )
            Spacer(modifier = Modifier.width(0.dp))
        }
    }
}

private data class OrderFilter(
    val status: InvoiceStatus?,
    val label: String
)

private fun Invoice.hasQrPaymentData(): Boolean {
    val finalAmount = finalPrice?.toDoubleOrNull() ?: 0.0
    return orderCode.isNotBlank() &&
        finalAmount > 0.0 &&
        !shopBankCode.isNullOrBlank() &&
        !shopBankAccount.isNullOrBlank()
}
