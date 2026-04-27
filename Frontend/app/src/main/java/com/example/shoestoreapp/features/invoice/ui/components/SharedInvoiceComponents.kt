package com.example.shoestoreapp.features.invoice.ui.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.example.shoestoreapp.features.admin.invoice.ui.components.formatAdminDate
import com.example.shoestoreapp.features.admin.invoice.ui.components.formatAdminPrice
import com.example.shoestoreapp.features.invoice.model.Detail
import com.example.shoestoreapp.features.invoice.model.Invoice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedInvoiceDetailsBottomSheet(
    isLoading: Boolean,
    invoice: Invoice,
    details: List<Detail>,
    onDismissRequest: () -> Unit,
    enablePhoneCall: Boolean = false // Enables phone call button.
) {
    val context = LocalContext.current

    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text("Order details", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(modifier = Modifier.height(12.dp))

                    val hasPhone = invoice.phones?.isNotBlank() == true
                    SharedDetailMetaRow(
                        label = "Phone",
                        value = invoice.phones.orEmpty().ifBlank { "-" },
                        isClickable = enablePhoneCall && hasPhone,
                        onClick = {
                            if (enablePhoneCall && hasPhone) {
                                val phone = invoice.phones.trim().orEmpty()
                                val dialIntent = Intent(Intent.ACTION_DIAL).apply { data = "tel:$phone".toUri() }
                                context.startActivity(dialIntent)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    SharedDetailMetaRow(label = "Address", value = invoice.address.orEmpty().ifBlank { "-" })
                    Spacer(modifier = Modifier.height(6.dp))
                    SharedDetailMetaRow(label = "Created", value = formatAdminDate(invoice.createdAt.orEmpty().ifBlank { "-" }))
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        Text("Product", modifier = Modifier.weight(2f), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Text("Qty", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold, fontSize = 16.sp, textAlign = TextAlign.Center)
                        Text("Unit price", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold, fontSize = 16.sp, textAlign = TextAlign.End)
                    }
                    HorizontalDivider(color = Color.LightGray)

                    if (details.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            Text("No details found for this order.", color = Color(0xFF6D6D6D), fontSize = 16.sp, modifier = Modifier.align(Alignment.Center))
                        }
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(details) { detail ->
                                SharedInvoiceDetailRow(detail = detail)
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
fun SharedInvoiceDetailRow(detail: Detail) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Row(modifier = Modifier.weight(2f), verticalAlignment = Alignment.CenterVertically) {
            val imgModifier = Modifier.size(52.dp).background(Color(0xFFF1F1F1), RoundedCornerShape(8.dp)).border(1.dp, Color(0xFFE7E7E7), RoundedCornerShape(8.dp))
            if (detail.imageUrl.isBlank()) {
                Box(modifier = imgModifier)
            } else {
                AsyncImage(model = detail.imageUrl, contentDescription = detail.productName, contentScale = ContentScale.Crop, modifier = imgModifier)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = detail.productName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "Color: ${detail.color} | Size: ${detail.size}", fontSize = 14.sp, color = Color.Gray)
            }
        }
        Text(text = "x${detail.quantity}", modifier = Modifier.weight(0.9f), fontSize = 16.sp, textAlign = TextAlign.Center)
        Text(text = formatAdminPrice(detail.unitPrice.toString()), modifier = Modifier.weight(1f), fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
    }
}

@Composable
fun SharedDetailMetaRow(label: String, value: String, isClickable: Boolean = false, onClick: (() -> Unit)? = null) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Text(text = "$label:", modifier = Modifier.width(72.dp), color = Color(0xFF666666), fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Text(
            text = value,
            modifier = Modifier.weight(1f).then(if (isClickable && onClick != null) Modifier.clickable { onClick() } else Modifier),
            color = if (isClickable) Color(0xFF1976D2) else Color(0xFF2E2E2E),
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            textDecoration = if (isClickable) TextDecoration.Underline else TextDecoration.None
        )
    }
}