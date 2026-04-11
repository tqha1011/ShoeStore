package com.example.shoestoreapp.features.user.invoice.ui

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.shoestoreapp.features.invoice.model.Invoice
import com.example.shoestoreapp.features.invoice.model.InvoiceStatus
import com.example.shoestoreapp.features.invoice.model.PaymentMethod
import com.example.shoestoreapp.features.invoice.ui.components.InvoiceStatusFilterChips
import com.example.shoestoreapp.features.user.invoice.viewmodel.UserInvoiceViewModel
import com.example.shoestoreapp.features.user.product.ui.components.BottomNavBar
import com.example.shoestoreapp.features.user.product.ui.components.BottomNavTab

@Composable
fun UserInvoiceScreen(
    viewModel: UserInvoiceViewModel = UserInvoiceViewModel(),
    onTabSelected: (BottomNavTab) -> Unit = {}
) {
    val selectedStatus by viewModel.selectedStatus.collectAsState()
    val invoices by viewModel.visibleInvoices.collectAsState()

    Scaffold(
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
                .padding(paddingValues)
                .background(Color.White)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "My Orders",
                modifier = Modifier.padding(top = 16.dp, bottom = 12.dp),
                color = Color.Black,
                fontWeight = FontWeight.Black,
                fontSize = 28.sp
            )

            UserInvoiceFilterRow(
                selectedStatus = selectedStatus,
                onFilterSelected = viewModel::onFilterChange
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(invoices) { invoice ->
                    UserInvoiceCard(invoice = invoice)
                }
            }
        }
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
        chipCornerRadius = 18.dp,
        chipHorizontalPadding = 12.dp,
        chipVerticalPadding = 7.dp,
        unselectedTextColor = Color(0xFF888888),
        showSelectedBorder = true,
        letterSpacing = 0.sp
    )
}

@Composable
private fun UserInvoiceCard(invoice: Invoice) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                        text = invoice.orderCode,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFF9D9D9D)
                    )
                    Text(
                        text = invoice.createdAt,
                        fontSize = 12.sp,
                        color = Color(0xFF6A6A6A)
                    )
                    Text(
                        text = if (invoice.paymentMethod == PaymentMethod.ONLINE) "ONLINE" else "COD",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = Color(0xFF777777)
                    )
                }
                Text(
                    text = "$${"%.2f".format(invoice.finalPrice)}",
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
            }

            Text(
                text = invoice.shippingAddress,
                modifier = Modifier.padding(top = 8.dp),
                color = Color(0xFF555555),
                fontSize = 12.sp
            )

            Text(
                text = "Status: ${invoice.status.name}",
                modifier = Modifier.padding(top = 6.dp),
                color = when (invoice.status) {
                    InvoiceStatus.PENDING -> Color(0xFF666666)
                    InvoiceStatus.PAID -> Color(0xFF1F5FAE)
                    InvoiceStatus.DELIVERING -> Color.Black
                    InvoiceStatus.DELIVERED -> Color(0xFF1E7D32)
                    InvoiceStatus.CANCELED -> Color(0xFFB3261E)
                },
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}


