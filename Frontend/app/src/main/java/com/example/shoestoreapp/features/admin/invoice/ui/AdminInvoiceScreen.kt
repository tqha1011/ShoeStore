package com.example.shoestoreapp.features.admin.invoice.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shoestoreapp.features.admin.invoice.ui.components.AdminInvoiceFilterChips
import com.example.shoestoreapp.features.admin.invoice.viewmodel.AdminInvoiceViewmodel
import com.example.shoestoreapp.features.admin.product.ui.components.AdminBottomNavBar
import com.example.shoestoreapp.features.admin.product.ui.components.AdminBottomNavTab
import com.example.shoestoreapp.features.admin.invoice.ui.components.AdminOrderCard
import com.example.shoestoreapp.features.invoice.model.Invoice
import com.example.shoestoreapp.features.invoice.model.InvoiceStatus
import com.example.shoestoreapp.features.invoice.model.nextWorkflowStatus
import com.example.shoestoreapp.features.invoice.ui.components.SharedInvoiceDetailsBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminInvoiceScreen(
    viewModel: AdminInvoiceViewmodel,
    onTabSelected: (AdminBottomNavTab) -> Unit = {}
) {
    val state = viewModel.state
    var selectedStatus by remember { mutableStateOf<InvoiceStatus?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    val visibleInvoices = if (selectedStatus == null) state.invoices else state.invoices.filter { it.status == selectedStatus }

    LaunchedEffect(state.error, state.successMessage) {
        state.error?.let { snackbarHostState.showSnackbar(it); viewModel.clearTransientMessage() }
        state.successMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearTransientMessage() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = { AdminTopBar() },
        bottomBar = { AdminBottomNavBar(selectedTab = AdminBottomNavTab.ORDERS, onTabSelected = onTabSelected) }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).background(Color.White)) {
            AdminInvoiceFilterChips(selectedStatus = selectedStatus, onFilterSelected = { selectedStatus = it })


            AdminInvoiceListContent(
                isLoading = state.isLoading,
                error = state.error,
                invoices = visibleInvoices,
                onStatusSelected = { invoice, targetStatus -> viewModel.updateInvoiceStatus(invoice, targetStatus) },
                onDetailsClick = { viewModel.openInvoiceDetails(it) }
            )
        }
    }

    // Bottom sheet is driven by selectedInvoice presence.
    if (state.selectedInvoice != null) {
        SharedInvoiceDetailsBottomSheet(
            isLoading = state.isDetailLoading,
            invoice = state.selectedInvoice,
            details = state.invoiceDetails,
            onDismissRequest = { viewModel.clearDetails() },
            enablePhoneCall = true // Enable phone call button
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTopBar() {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "SHOE STORE",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color.Black,
                letterSpacing = 2.sp
            )
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.White,
            titleContentColor = Color.Black,
            navigationIconContentColor = Color.Black,
            actionIconContentColor = Color.Black
        ),
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(top = 12.dp)
    )
}


@Composable
fun AdminInvoiceListContent(
    isLoading: Boolean, error: String?, invoices: List<Invoice>,
    onStatusSelected: (Invoice, InvoiceStatus) -> Unit, onDetailsClick: (Invoice) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading && invoices.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (error != null && invoices.isEmpty()) {
            Text(text = "Error: $error", color = Color.Red, modifier = Modifier.align(Alignment.Center).padding(16.dp))
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(invoices) { invoice ->
                    AdminOrderCard(
                        invoice = invoice, statusOptions = listOfNotNull(invoice.nextWorkflowStatus()),
                        onStatusSelected = { onStatusSelected(invoice, it) }, onDetailsClick = { onDetailsClick(invoice) }
                    )
                }
            }
        }
    }
}