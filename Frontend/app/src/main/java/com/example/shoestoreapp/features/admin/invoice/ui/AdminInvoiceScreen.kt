package com.example.shoestoreapp.features.admin.invoice.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shoestoreapp.features.admin.invoice.ui.components.AdminInvoiceFilterChips
import com.example.shoestoreapp.features.admin.invoice.viewmodel.AdminInvoiceViewmodel
import com.example.shoestoreapp.features.admin.product.ui.components.AdminBottomNavBar
import com.example.shoestoreapp.features.admin.product.ui.components.AdminBottomNavTab
import com.example.shoestoreapp.features.admin.invoice.ui.components.AdminOrderCard
import com.example.shoestoreapp.features.invoice.model.Detail
import com.example.shoestoreapp.features.invoice.model.InvoiceStatus
import com.example.shoestoreapp.features.invoice.model.nextWorkflowStatus
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminInvoiceScreen(
    // BOMB 1 FIXED: Do not initialize "= AdminInvoiceViewModel()" here anymore.
    // Inject it from NavGraph or MainActivity to avoid crashes.
    viewModel: AdminInvoiceViewmodel,
    onTabSelected: (AdminBottomNavTab) -> Unit = {}
) {
    val context = LocalContext.current

    // 1. GET STATE FROM API (The new data engine)
    val state = viewModel.state

    // 2. KEEP YOUR UI STATE INTACT
    // Note: Since the new state doesn't have a filter variable yet, manage it locally here.
    var selectedStatus by remember { mutableStateOf<InvoiceStatus?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Filter the list based on UI selection (Assuming API returns all items initially)
    val visibleInvoices = if (selectedStatus == null) {
        state.invoices
    } else {
        state.invoices.filter { it.status == selectedStatus }
    }

    LaunchedEffect(state.error, state.successMessage) {
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
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.Black)
                Text(
                    text = "SHOE STORE",
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.6.sp
                )
                Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF666666))
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

            // Keep Filter Chips as is
            AdminInvoiceFilterChips(
                selectedStatus = selectedStatus,
                onFilterSelected = { newStatus -> selectedStatus = newStatus }
            )

            // ==========================================
            // HANDLE 3 STATES: LOADING - ERROR - SUCCESS
            // ==========================================
            Box(modifier = Modifier.fillMaxSize()) {
                if (state.isLoading && state.invoices.isEmpty()) {
                    // Initial data loading -> Show spinner
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (state.error != null && state.invoices.isEmpty()) {
                    // API Error -> Show error message
                    Text(
                        text = "Error: ${state.error}",
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                } else {
                    // Success -> Render your List Card UI
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(visibleInvoices) { invoice ->
                            AdminOrderCard(
                                invoice = invoice,
                                statusOptions = listOfNotNull(invoice.nextWorkflowStatus()),
                                onStatusSelected = { targetStatus ->
                                    viewModel.updateInvoiceStatus(invoice, targetStatus)
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

    // ====================================================
    // BOTTOM SHEET FOR INVOICE DETAILS
    // ====================================================
    if (state.selectedInvoice != null) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.clearDetails() }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.7f) // Chiếm 70% màn hình
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                if (state.isDetailLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text("Order details", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))

                        val selectedInvoice = state.selectedInvoice!!
                        DetailMetaRow(
                            label = "Phone",
                            value = selectedInvoice.phones.orEmpty().ifBlank { "-" },
                            isClickable = selectedInvoice.phones?.isNotBlank() == true,
                            onClick = {
                                val phone = selectedInvoice.phones?.trim().orEmpty()
                                if (phone.isNotEmpty()) {
                                    // Launch phone dialer
                                    val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:$phone")
                                    }
                                    // Launch the intent
                                    context.startActivity(dialIntent)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        DetailMetaRow(
                            label = "Address",
                            value = selectedInvoice.address.orEmpty().ifBlank { "-" }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        DetailMetaRow(
                            label = "Created",
                            value = selectedInvoice.createdAt.orEmpty().ifBlank { "-" }
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                            Text("Product", modifier = Modifier.weight(2f), fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                            Text("Qty", modifier = Modifier.weight(0.55f), fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                            Text("Unit price", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                        }
                        HorizontalDivider(color = Color.LightGray)

                        if (state.invoiceDetails.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Text(
                                    text = "No details found for this order.",
                                                    color = Color(0xFF6D6D6D),
                                                    fontSize = 17.sp,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        } else {
                            LazyColumn(modifier = Modifier.weight(1f)) {
                                items(state.invoiceDetails) { detail ->
                                    InvoiceDetailRow(detail = detail)
                                    HorizontalDivider(color = Color(0xFFF0F0F0))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun InvoiceDetailRow(detail: Detail) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(2f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (detail.imageUrl.isBlank()) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(Color(0xFFF1F1F1), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFFE7E7E7), RoundedCornerShape(8.dp))
                )
            } else {
                AsyncImage(
                    model = detail.imageUrl,
                    contentDescription = detail.productName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(52.dp)
                        .background(Color(0xFFF1F1F1), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFFE7E7E7), RoundedCornerShape(8.dp))
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = detail.productName, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(text = "Color: ${detail.color} | Size: ${detail.size}", fontSize = 15.sp, color = Color.Gray)
            }
        }

        Text(
            text = "x${detail.quantity}",
            modifier = Modifier.weight(0.55f),
            fontSize = 17.sp
        )
        Text(
            text = "${detail.unitPrice} đ",
            modifier = Modifier.weight(1f),
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DetailMetaRow(
    label: String,
    value: String,
    isClickable: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Text(
            text = "$label:",
            modifier = Modifier.width(72.dp),
            color = Color(0xFF666666),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            modifier = Modifier
                .weight(1f)
                .then(
                    if (isClickable && onClick != null) {
                        Modifier.clickable { onClick() }
                    } else {
                        Modifier
                    }
                ),
            color = if (isClickable) Color(0xFF1976D2) else Color(0xFF2E2E2E),
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            textDecoration = if (isClickable) TextDecoration.Underline else TextDecoration.None
        )
    }
}