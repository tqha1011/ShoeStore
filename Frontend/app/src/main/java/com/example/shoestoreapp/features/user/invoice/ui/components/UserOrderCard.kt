package com.example.shoestoreapp.features.user.invoice.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shoestoreapp.features.invoice.model.Invoice
import com.example.shoestoreapp.features.invoice.ui.components.InvoiceCardContainer
import com.example.shoestoreapp.features.invoice.ui.components.InvoiceStatusOrUnknown
import com.example.shoestoreapp.features.invoice.ui.components.invoiceTextOrDash

@Composable
fun UserOrderCard(
    invoice: Invoice,
    onDetailsClick: () -> Unit
) {
    val paymentMethodText = invoiceTextOrDash(invoice.paymentMethod)
    val createdAtText = invoiceTextOrDash(invoice.createdAt)
    val finalPriceText = invoiceTextOrDash(invoice.finalPrice)

    InvoiceCardContainer {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = invoice.orderCode,
                        color = Color(0xFF757575),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    InvoiceStatusOrUnknown(status = invoice.status, unknownFontSize = 11.sp)
                }

                Spacer(modifier = Modifier.size(4.dp))

                Text(
                    text = paymentMethodText,
                    color = Color(0xFF666666),
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.size(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = createdAtText,
                            color = Color(0xFF6B6B6B),
                            fontSize = 12.sp
                        )
                        Text(
                            text = finalPriceText,
                            color = Color.Black,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Button(
                        onClick = onDetailsClick,
                        modifier = Modifier.height(34.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = "View Details",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

