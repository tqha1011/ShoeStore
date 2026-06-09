package com.example.shoestoreapp.features.user.invoice.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.shoestoreapp.features.admin.invoice.ui.components.formatAdminDate
import com.example.shoestoreapp.features.admin.invoice.ui.components.formatAdminPrice
import com.example.shoestoreapp.features.invoice.model.Detail
import com.example.shoestoreapp.features.invoice.model.Invoice
import com.example.shoestoreapp.features.invoice.model.InvoiceStatus
import com.example.shoestoreapp.features.invoice.ui.components.invoiceTextOrDash

@Composable
fun UserOrderCard(
    invoice: Invoice,
    previewDetail: Detail? = null,
    isCancelling: Boolean = false,
    onCancelClick: () -> Unit = {},
    onDetailsClick: () -> Unit
) {
    val paymentMethodText = invoiceTextOrDash(invoice.paymentMethod)
    val createdAtText = formatAdminDate(invoiceTextOrDash(invoice.createdAt))
    val finalPriceText = formatAdminPrice(invoiceTextOrDash(invoice.finalPrice))
    val title = previewDetail?.productName?.takeIf { it.isNotBlank() } ?: "Order ${invoice.orderCode}"
    val description = previewDetail?.let { detail ->
        listOf(
            detail.color.takeIf { it.isNotBlank() }?.let { "Color $it" },
            "Size ${detail.size}",
            "x${detail.quantity}"
        ).filterNotNull().joinToString(", ")
    } ?: "$paymentMethodText - $createdAtText"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#${invoice.orderCode}",
                    color = Color(0xFF817B7D),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OrderStatusPill(status = invoice.status)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OrderPreviewImage(
                    imageUrl = previewDetail?.imageUrl.orEmpty(),
                    contentDescription = title
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        color = Color(0xFF3F383B),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFF0EDEF))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "TOTAL PAYMENT",
                        color = Color(0xFF857F82),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.6.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = finalPriceText,
                        color = Color.Black,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(horizontalAlignment = Alignment.End) {
                    OutlinedButton(
                        onClick = onDetailsClick,
                        modifier = Modifier.height(42.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.4.dp, Color.Black),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = "View details",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    if (invoice.status == InvoiceStatus.PENDING) {
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedButton(
                            onClick = onCancelClick,
                            enabled = !isCancelling,
                            modifier = Modifier.height(32.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFD6D1D4)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF5D5659)
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = if (isCancelling) "Cancelling..." else "Cancel",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderPreviewImage(
    imageUrl: String,
    contentDescription: String
) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF1F1F1)),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl.isBlank()) {
            Text(
                text = "KicksHub",
                color = Color(0xFF8A8588),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        } else {
            AsyncImage(
                model = imageUrl,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        }
    }
}

@Composable
private fun OrderStatusPill(status: InvoiceStatus?) {
    val (label, backgroundColor, textColor) = when (status) {
        InvoiceStatus.PENDING -> Triple("PENDING", Color(0xFFFFF2D6), Color(0xFFB07A00))
        InvoiceStatus.PAID -> Triple("PAID", Color(0xFFEAF3FF), Color(0xFF2174E8))
        InvoiceStatus.DELIVERING -> Triple("DELIVERING", Color(0xFFEAF3FF), Color(0xFF2174E8))
        InvoiceStatus.DELIVERED -> Triple("COMPLETED", Color(0xFFE5F6EA), Color(0xFF11823B))
        InvoiceStatus.CANCELLED -> Triple("CANCELLED", Color(0xFFFFE8E8), Color(0xFFC62828))
        null -> Triple("UNKNOWN", Color(0xFFF0EEF0), Color(0xFF6A6266))
    }

    Text(
        text = label,
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        color = textColor,
        fontSize = 10.sp,
        fontWeight = FontWeight.ExtraBold,
        maxLines = 1
    )
}
