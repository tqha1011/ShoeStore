package com.example.shoestoreapp.features.user.invoice.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
    val subtotal = state.invoiceDetails.sumOf { detail ->
        detail.unitPrice.toLong() * detail.quantity.toLong()
    }
    val totalText = formatAdminPrice(selectedInvoice.finalPrice.orEmpty())

    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            if (state.isDetailLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        item {
                            Text(
                                text = "Order details",
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.Black
                            )
                        }

                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                UserDetailMetaRow(
                                    label = "Phone",
                                    value = selectedInvoice.phones.orEmpty().ifBlank { "-" }
                                )
                                UserDetailMetaRow(
                                    label = "Address",
                                    value = selectedInvoice.address.orEmpty().ifBlank { "-" }
                                )
                                UserDetailMetaRow(
                                    label = "Created",
                                    value = formatAdminDate(selectedInvoice.createdAt.orEmpty().ifBlank { "-" })
                                )
                            }
                        }

                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "Product",
                                        modifier = Modifier.weight(2.2f),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "Qty",
                                        modifier = Modifier.weight(0.7f),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "Unit price",
                                        modifier = Modifier.weight(1.3f),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp,
                                        textAlign = TextAlign.End
                                    )
                                }
                                HorizontalDivider(color = Color(0xFFD8CED1))
                            }
                        }

                        if (state.invoiceDetails.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                ) {
                                    Text(
                                        text = "No details found for this order.",
                                        color = Color(0xFF6D6D6D),
                                        fontSize = 16.sp,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }
                        } else {
                            items(state.invoiceDetails) { detail ->
                                UserInvoiceDetailRow(detail = detail)
                            }
                        }

                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                HorizontalDivider(color = Color(0xFFD8CED1))
                                PriceSummaryRow(
                                    label = "Subtotal",
                                    value = formatAdminPrice(subtotal.toString())
                                )
                                PriceSummaryRow(
                                    label = "Payment method",
                                    value = selectedInvoice.paymentMethod.orEmpty().ifBlank { "-" }
                                )
                                HorizontalDivider(color = Color(0xFFD8CED1))
                                PriceSummaryRow(
                                    label = "Total",
                                    value = totalText,
                                    labelWeight = FontWeight.Black,
                                    valueWeight = FontWeight.Black,
                                    valueSize = 25.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp, bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {},
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFF6F676B)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.White,
                                contentColor = Color.Black
                            )
                        ) {
                            Text(
                                text = "Contact support",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1
                            )
                        }

                        Button(
                            onClick = {},
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Black,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text(
                                text = "Track order",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1
                            )
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
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(2.2f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProductThumbnail(detail = detail)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = detail.productName,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Color ${detail.color}, Size ${detail.size}",
                    fontSize = 12.sp,
                    color = Color(0xFF6B6468),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Text(
            text = detail.quantity.toString(),
            modifier = Modifier.weight(0.7f),
            color = Color.Black,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        Text(
            text = formatAdminPrice(detail.unitPrice.toString()),
            modifier = Modifier.weight(1.3f),
            color = Color.Black,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun ProductThumbnail(detail: Detail) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF3F3F3)),
        contentAlignment = Alignment.Center
    ) {
        if (detail.imageUrl.isBlank()) {
            Text(
                text = "KicksHub",
                color = Color(0xFF8A8588),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        } else {
            AsyncImage(
                model = detail.imageUrl,
                contentDescription = detail.productName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        }
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
            modifier = Modifier.width(96.dp),
            color = Color(0xFF484044),
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            color = Color.Black,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PriceSummaryRow(
    label: String,
    value: String,
    labelWeight: FontWeight = FontWeight.Medium,
    valueWeight: FontWeight = FontWeight.Medium,
    valueSize: androidx.compose.ui.unit.TextUnit = 18.sp
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color(0xFF4C4448),
            fontSize = 17.sp,
            fontWeight = labelWeight
        )
        Text(
            text = value,
            color = Color.Black,
            fontSize = valueSize,
            fontWeight = valueWeight,
            textAlign = TextAlign.End
        )
    }
}
