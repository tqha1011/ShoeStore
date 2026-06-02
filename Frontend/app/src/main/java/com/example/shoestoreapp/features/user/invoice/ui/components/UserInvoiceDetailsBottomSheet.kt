package com.example.shoestoreapp.features.user.invoice.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.shoestoreapp.features.admin.invoice.ui.components.formatAdminDate
import com.example.shoestoreapp.features.admin.invoice.ui.components.formatAdminPrice
import com.example.shoestoreapp.features.invoice.model.Detail
import com.example.shoestoreapp.features.user.invoice.viewmodel.UserInvoiceState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserInvoiceDetailsBottomSheet(
    state: UserInvoiceState,
    onDismissRequest: () -> Unit
) {
    val selectedInvoice = state.selectedInvoice ?: return

    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (state.isDetailLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "Order details",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    UserDetailMetaRow(
                        label = "Phone",
                        value = selectedInvoice.phones.orEmpty().ifBlank { "-" }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    UserDetailMetaRow(
                        label = "Address",
                        value = selectedInvoice.address.orEmpty().ifBlank { "-" }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    UserDetailMetaRow(
                        label = "Created",
                        value = formatAdminDate(selectedInvoice.createdAt.orEmpty().ifBlank { "-" })
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        Text(
                            text = "Product",
                            modifier = Modifier.weight(2f),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Qty",
                            modifier = Modifier.weight(1.5f),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Unit price",
                            modifier = Modifier.weight(1.5f),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            textAlign = TextAlign.End
                        )
                    }
                    HorizontalDivider(color = Color.LightGray)

                    if (state.invoiceDetails.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            Text(
                                text = "No details found for this order.",
                                color = Color(0xFF6D6D6D),
                                fontSize = 16.sp,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(state.invoiceDetails) { detail ->
                                UserInvoiceDetailRow(detail = detail)
                                HorizontalDivider(color = Color(0xFFF0F0F0))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserInvoiceDetailRow(detail: Detail) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1.9f),
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
                Text(text = detail.productName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    text = "Color: ${detail.color} | Size: ${detail.size}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }

        Text(
            text = "x${detail.quantity}",
            modifier = Modifier.weight(0.5f),
            fontSize = 16.sp
        )
        Text(
            text = formatAdminPrice(detail.unitPrice.toString()),
            modifier = Modifier.weight(1.2f),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun UserDetailMetaRow(
    label: String,
    value: String
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
            modifier = Modifier.weight(1f),
            color = Color(0xFF2E2E2E),
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
