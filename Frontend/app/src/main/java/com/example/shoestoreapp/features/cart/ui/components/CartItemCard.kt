package com.example.shoestoreapp.features.cart.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.shoestoreapp.features.cart.data.models.CartItem
import androidx.compose.ui.draw.alpha

/**
 * CartItemCard Component
 * Hiển thị thông tin chi tiết của một item trong giỏ
 *
 * @param item - CartItem object
 * @param onIncreaseQuantity - Callback tăng số lượng (nhận String GUID)
 * @param onDecreaseQuantity - Callback giảm số lượng (nhận String GUID)
 * @param onRemove - Callback xóa item (nhận String GUID)
 */
@Composable
fun CartItemCard(
    item: CartItem,
    onIncreaseQuantity: (String) -> Unit = {},
    onDecreaseQuantity: (String) -> Unit = {},
    onRemove: (String) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(Color.White)
            .alpha(if (item.stock <= 0) 0.5f else 1f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            ProductImage(item = item)

            CartItemDetails(
                item = item,
                onIncreaseQuantity = onIncreaseQuantity,
                onDecreaseQuantity = onDecreaseQuantity,
                onRemove = onRemove,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ProductImage(item: CartItem) {
    AsyncImage(
        model = item.imageUrl,
        contentDescription = item.name,
        modifier = Modifier
            .size(120.dp)
            .background(
                Color(0xFFF5F5F5),
                shape = RoundedCornerShape(4.dp)
            ),
        contentScale = ContentScale.Crop
    )
}

@Composable
private fun CartItemDetails(
    item: CartItem,
    onIncreaseQuantity: (String) -> Unit,
    onDecreaseQuantity: (String) -> Unit,
    onRemove: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        ProductTitle(item.name)
        ProductDescription(item.description)
        StockStatusDisplay(stock = item.stock)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SizeDisplay(item.size)
            QuantitySelector(
                item = item,
                onIncrease = onIncreaseQuantity,
                onDecrease = onDecreaseQuantity
            )
        }

        PriceDisplay(item.price)
        DeleteButton(item = item, onRemove = onRemove)
    }
}

@Composable
private fun ProductTitle(name: String) {
    Text(
        text = name,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black
    )
}

@Composable
private fun ProductDescription(description: String) {
    Text(
        text = description,
        fontSize = 12.sp,
        color = Color.Gray,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun StockStatusDisplay(stock: Int) {
    when {
        stock <= 0 -> {
            Text(
                text = "Out of Stock",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFBA1A1A),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        stock < 3 -> {
            Text(
                text = "Just a few left",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFBA1A1A),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun SizeDisplay(size: String) {
    Text(
        text = "Size $size",
        fontSize = 12.sp,
        color = Color.Gray
    )
}

@Composable
private fun QuantitySelector(
    item: CartItem,
    onIncrease: (String) -> Unit,
    onDecrease: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .border(
                width = 1.dp,
                color = Color.LightGray,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DecreaseButton(item = item, onDecrease = onDecrease)
        QuantityText(item.quantity)
        IncreaseButton(item = item, onIncrease = onIncrease)
    }
}

@Composable
private fun DecreaseButton(item: CartItem, onDecrease: (String) -> Unit) {
    IconButton(
        onClick = { onDecrease(item.id.toString()) },
        enabled = item.stock > 0,
        modifier = Modifier.size(24.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Remove,
            contentDescription = "Decrease",
            modifier = Modifier.size(16.dp),
            tint = if (item.stock > 0) Color.Gray else Color.LightGray
        )
    }
}

@Composable
private fun QuantityText(quantity: Int) {
    Text(
        text = quantity.toString(),
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium
    )
}

@Composable
private fun IncreaseButton(item: CartItem, onIncrease: (String) -> Unit) {
    IconButton(
        onClick = { onIncrease(item.id.toString()) },
        enabled = item.stock > 0,
        modifier = Modifier.size(24.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "Increase",
            modifier = Modifier.size(16.dp),
            tint = if (item.stock > 0) Color.Gray else Color.LightGray
        )
    }
}

@Composable
private fun PriceDisplay(price: Double) {
    Text(
        text = "$${price}",
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun DeleteButton(item: CartItem, onRemove: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        IconButton(
            onClick = { onRemove(item.id.toString()) },
            modifier = Modifier.size(16.dp)
        ) {
            Icon(
                painter = painterResource(id = android.R.drawable.ic_menu_delete),
                contentDescription = "Delete",
                tint = Color.Black
            )
        }
    }
}


