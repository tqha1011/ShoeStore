package com.example.shoestoreapp.features.user.profile.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shoestoreapp.features.invoice.model.InvoiceStatus

@Composable
fun UserOrdersSection(
    orderCounts: Map<InvoiceStatus, Int>,
    onStatusClick: (InvoiceStatus) -> Unit,
    onViewHistoryClick: () -> Unit
) {
    val statusItems = listOf(
        OrderStatusItem(InvoiceStatus.PENDING, "Pending", Icons.Default.Inventory),
        OrderStatusItem(InvoiceStatus.PAID, "Paid", Icons.Default.Payments),
        OrderStatusItem(InvoiceStatus.DELIVERING, "Delivering", Icons.Default.LocalShipping),
        OrderStatusItem(InvoiceStatus.DELIVERED, "Delivered", Icons.Default.CheckCircle),
        OrderStatusItem(InvoiceStatus.CANCELLED, "Cancelled", Icons.Default.Cancel)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEAEAEA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Orders",
                    color = Color.Black,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "View Purchase History >",
                    color = Color(0xFF7A7A7A),
                    fontSize = 14.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable(onClick = onViewHistoryClick)
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    textDecoration = TextDecoration.None
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                statusItems.forEach { item ->
                    OrderStatusAction(
                        label = item.label,
                        icon = item.icon,
                        badgeCount = orderCounts[item.status] ?: 0,
                        onClick = { onStatusClick(item.status) }
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderStatusAction(
    label: String,
    icon: ImageVector,
    badgeCount: Int,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp, vertical = 2.dp)
    ) {
        BadgedBox(
            badge = {
                if (badgeCount > 0) {
                    Badge(
                        containerColor = Color(0xFFE53935),
                        contentColor = Color.White
                    ) {
                        Text(
                            text = badgeCount.coerceAtMost(99).toString(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFF333333),
                modifier = Modifier.size(24.dp)
            )
        }

        Text(
            text = label,
            color = Color(0xFF4A4A4A),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

private data class OrderStatusItem(
    val status: InvoiceStatus,
    val label: String,
    val icon: ImageVector
)
