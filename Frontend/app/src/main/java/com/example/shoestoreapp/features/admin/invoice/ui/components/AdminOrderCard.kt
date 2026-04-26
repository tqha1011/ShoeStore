package com.example.shoestoreapp.features.admin.invoice.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shoestoreapp.features.invoice.model.Invoice
import com.example.shoestoreapp.features.invoice.model.InvoiceStatus
import com.example.shoestoreapp.features.invoice.model.displayName
import com.example.shoestoreapp.features.invoice.ui.components.InvoiceCardContainer
import com.example.shoestoreapp.features.invoice.ui.components.InvoiceStatusOrUnknown
import com.example.shoestoreapp.features.invoice.ui.components.invoiceTextOrDash
import java.util.Locale

fun formatAdminDate(isoString: String?): String {
    if (isoString.isNullOrEmpty() || isoString == "-") return "-"
    return try {
        val datePart = isoString.substringBefore("T")
        val dateTokens = datePart.split("-")
        if (dateTokens.size < 3) return isoString

        val month = dateTokens[1]
        val day = dateTokens[2]
        val rawTime = isoString.substringAfter("T", "")
        val timeTokens = rawTime.split(":")
        val timePart = if (timeTokens.size >= 2) {
            "${timeTokens[0]}:${timeTokens[1]}"
        } else {
            "--:--"
        }

        "$timePart - $day/$month"
    } catch (_: Exception) {
        isoString
    }
}

fun formatAdminPrice(priceStr: String?): String {
    if (priceStr.isNullOrEmpty() || priceStr == "-") return "0 đ"
    val price = priceStr.toDoubleOrNull() ?: 0.0
    return String.format(Locale.US, "%,.0f đ", price).replace(",", ".")
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AdminOrderCard(
    invoice: Invoice,
    statusOptions: List<InvoiceStatus>,
    onStatusSelected: (InvoiceStatus) -> Unit,
    onDetailsClick: () -> Unit
) {
    var isStatusMenuExpanded by remember { mutableStateOf(false) }
    var isQuickInfoVisible by remember { mutableStateOf(false) }

    val paymentMethodText = invoiceTextOrDash(invoice.paymentMethod)
    val createdAtText = formatAdminDate(invoiceTextOrDash(invoice.createdAt))
    val finalPriceText = formatAdminPrice(invoiceTextOrDash(invoice.finalPrice))
    val phoneText = invoiceTextOrDash(invoice.phones)
    val addressText = invoiceTextOrDash(invoice.address)

    InvoiceCardContainer {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = {
                            if (isQuickInfoVisible) isQuickInfoVisible = false
                        },
                        onLongClick = { isQuickInfoVisible = true }
                    )
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    PaymentMethodBadge(paymentMethod = paymentMethodText)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = invoice.userName,
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    InvoiceStatusOrUnknown(status = invoice.status)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = createdAtText,
                        color = Color(0xFF6D6D6D),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = finalPriceText,
                        color = Color.Black,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDetailsClick,
                        modifier = Modifier
                            .weight(0.38f)
                            .height(42.dp),
                        border = BorderStroke(1.dp, Color(0xFF1F1F1F)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "Details", color = Color.Black, maxLines = 1)
                    }

                    Box(modifier = Modifier.weight(0.62f)) {
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
                                Text(text = "Update Status", maxLines = 1)
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

            if (isQuickInfoVisible) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.48f))
                        .combinedClickable(
                            onClick = { isQuickInfoVisible = false },
                            onLongClick = { isQuickInfoVisible = false }
                        )
                        .padding(10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Order Quick Details",
                            color = Color.Black,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        QuickInfoRow(label = "Phone", value = phoneText)
                        QuickInfoRow(label = "Address", value = addressText)
                        QuickInfoRow(label = "Created", value = createdAtText)
                        Text(
                            text = "Tap to close",
                            color = Color(0xFF777777),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickInfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "$label:",
            color = Color(0xFF555555),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            color = Color.Black,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
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
