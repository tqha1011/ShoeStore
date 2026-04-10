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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.shoestoreapp.features.invoice.model.Invoice
import com.example.shoestoreapp.features.invoice.model.InvoiceStatus
import com.example.shoestoreapp.features.invoice.model.PaymentMethod

@Composable
fun AdminInvoiceScreen(
    viewModel: AdminInvoiceViewModel = AdminInvoiceViewModel(),
    onTabSelected: (AdminBottomNavTab) -> Unit = {}
) {
    val selectedStatus by viewModel.selectedStatus.collectAsState()
    val invoices by viewModel.visibleInvoices.collectAsState()

    Scaffold(
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
                    val nextStatus = viewModel.getNextStatus(invoice)
                    InvoiceCard(
                        invoice = invoice,
                        nextStatus = nextStatus,
                        onUpdateStatus = { viewModel.advanceStatus(invoice.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun InvoiceCard(
    invoice: Invoice,
    nextStatus: InvoiceStatus?,
    onUpdateStatus: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E5E5))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "#${invoice.orderCode}",
                        fontSize = 11.sp,
                        color = Color(0xFF9D9D9D),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = invoice.fullName,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                    Text(
                        text = invoice.createdAt,
                        fontSize = 11.sp,
                        color = Color(0xFF7B7B7B)
                    )
                    Text(
                        text = if (invoice.paymentMethod == PaymentMethod.ONLINE) "ONLINE" else "COD",
                        fontSize = 11.sp,
                        color = Color(0xFF5E5E5E),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$${"%.2f".format(invoice.finalPrice)}",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                    StatusChip(status = invoice.status)
                }
            }

            Text(
                text = "${invoice.invoiceDetails.sumOf { it.quantity }} items - ${invoice.shippingAddress}",
                modifier = Modifier.padding(top = 10.dp, bottom = 12.dp),
                fontSize = 12.sp,
                color = Color(0xFF5E5E5E)
            )

            Button(
                onClick = onUpdateStatus,
                enabled = nextStatus != null,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                val buttonText = if (nextStatus == null) {
                    "No Action"
                } else {
                    "Mark ${nextStatus.name}"
                }
                Text(text = buttonText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun StatusChip(status: InvoiceStatus) {
    val bg = when (status) {
        InvoiceStatus.PENDING -> Color(0xFFF2F2F2)
        InvoiceStatus.PAID -> Color(0xFFE8F2FF)
        InvoiceStatus.DELIVERING -> Color.Black
        InvoiceStatus.DELIVERED -> Color(0xFFE9F9ED)
        InvoiceStatus.CANCELED -> Color(0xFFFFECEB)
    }
    val fg = when (status) {
        InvoiceStatus.PENDING -> Color(0xFF666666)
        InvoiceStatus.PAID -> Color(0xFF1F5FAE)
        InvoiceStatus.DELIVERING -> Color.White
        InvoiceStatus.DELIVERED -> Color(0xFF1E7D32)
        InvoiceStatus.CANCELED -> Color(0xFFB3261E)
    }

    Text(
        text = status.name,
        modifier = Modifier
            .padding(top = 4.dp)
            .background(bg, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        color = fg,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold
    )
}

