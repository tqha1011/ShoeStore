package com.example.shoestoreapp.features.admin.invoice.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.shoestoreapp.features.invoice.model.Invoice
import com.example.shoestoreapp.features.invoice.model.InvoiceStatus
import com.example.shoestoreapp.features.invoice.model.displayName
import com.example.shoestoreapp.features.invoice.ui.components.StatusBadge

@Composable
fun AdminOrderCard(
    invoice: Invoice,
    statusOptions: List<InvoiceStatus>,
    onStatusSelected: (InvoiceStatus) -> Unit,
    onDetailsClick: () -> Unit
) {
    var isStatusMenuExpanded by remember { mutableStateOf(false) }

    val paymentMethodText = invoice.paymentMethod?.trim().orEmpty().ifEmpty { "-" }
    val createdAtText = invoice.createdAt?.trim().orEmpty().ifEmpty { "-" }
    val finalPriceText = invoice.finalPrice?.trim().orEmpty().ifEmpty { "-" }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFFE5E5E5))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.width(100.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDetailsClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp),
                    border = BorderStroke(1.dp, Color(0xFF1F1F1F)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = "Details", color = Color.Black)
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = invoice.orderCode,
                        color = Color(0xFF8C8C8C),
                        fontSize = 11.sp,
                        letterSpacing = 0.8.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    PaymentMethodBadge(paymentMethod = paymentMethodText)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = invoice.userName,
                        color = Color.Black,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    invoice.status?.let { status ->
                        StatusBadge(status = status)
                    } ?: Text(
                        text = "Unknown",
                        color = Color(0xFF8C8C8C),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = createdAtText,
                        color = Color(0xFF6D6D6D),
                        fontSize = 12.sp
                    )
                    Text(
                        text = finalPriceText,
                        color = Color.Black,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box {
                    Button(
                        onClick = { isStatusMenuExpanded = true },
                        enabled = statusOptions.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            contentColor = Color.White
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(text = "Update Status")
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Update status",
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = isStatusMenuExpanded,
                        onDismissRequest = { isStatusMenuExpanded = false }
                    ) {
                        statusOptions.forEach { targetStatus ->
                            DropdownMenuItem(
                                text = { Text(text = targetStatus.displayName()) },
                                onClick = {
                                    isStatusMenuExpanded = false
                                    onStatusSelected(targetStatus)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderImage(imageUrl: String, size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF0F0F0))
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Order image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun PaymentMethodBadge(paymentMethod: String) {
    Text(
        text = paymentMethod,
        modifier = Modifier.padding(horizontal = 2.dp, vertical = 2.dp),
        color = Color(0xFF777777),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold
    )
}
