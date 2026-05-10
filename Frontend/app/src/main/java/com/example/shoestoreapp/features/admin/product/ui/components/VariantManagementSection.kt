package com.example.shoestoreapp.features.admin.product.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.shoestoreapp.features.admin.product.ProductVariant

@Composable
fun VariantManagementSection(
    variants: List<ProductVariant>,
    onAddClick: () -> Unit,
    onEditClick: (ProductVariant) -> Unit,
    onDeleteClick: (ProductVariant) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "Inventory & Variants",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.Black
            )
            Text(
                text = "${variants.size} total",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6B6B6B)
            )
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF6F6F6),
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFD0D0D0), RoundedCornerShape(12.dp))
            ) {
                VariantHeaderRow()
                variants.forEach { variant ->
                    VariantRow(
                        variant = variant,
                        onEditClick = { onEditClick(variant) },
                        onDeleteClick = { onDeleteClick(variant) }
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                // Draw dashed border for the "Add New Variant" button.
                .drawBehind {
                    val stroke = Stroke(
                        width = 2.dp.toPx(),
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                    drawRoundRect(
                        color = Color(0xFFBDBDBD),
                        size = size,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                        style = stroke
                    )
                }
                .padding(vertical = 14.dp)
                .background(Color.Transparent)
                .clickable(onClick = onAddClick)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Add New Variant",
                    modifier = Modifier.padding(start = 8.dp),
                    color = Color.Black,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun VariantHeaderRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF0F0F0))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = "Size",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF6B6B6B)
        )
        Text(
            text = "Color",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF6B6B6B)
        )
        Text(
            text = "Stock",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF6B6B6B)
        )
        Text(
            text = "Actions",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF6B6B6B),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun VariantRow(
    variant: ProductVariant,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val stockColor = if (variant.stock == 0) Color(0xFFBA1A1A) else Color.Black
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = variant.size,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black
        )
        Text(
            text = variant.color,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF6B6B6B)
        )
        Text(
            text = variant.stock.toString(),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = stockColor
        )
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = Color(0xFF6B6B6B)
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete",
                    tint = Color(0xFF6B6B6B)
                )
            }
        }
    }
}
