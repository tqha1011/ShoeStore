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

/**
 * CartItemCard Component
 * Hiển thị thông tin chi tiết của một item trong giỏ
 *
 * @param item - CartItem object
 * @param onIncreaseQuantity - Callback tăng số lượng
 * @param onDecreaseQuantity - Callback giảm số lượng
 * @param onRemove - Callback xóa item
 * @param onAddToWishlist - Callback thêm vào wishlist
 */
@Composable
fun CartItemCard(
    item: CartItem,
    onIncreaseQuantity: (Int) -> Unit = {},
    onDecreaseQuantity: (Int) -> Unit = {},
    onRemove: (Int) -> Unit = {},
    onAddToWishlist: (Int) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Product Image
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

            // Product Info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Text(
                    text = item.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Text(
                    text = item.description,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )

                // Check stock status
                if (item.stock < 3) {
                    Text(
                        text = "Just a few left",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFBA1A1A),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Size & Quantity & Price
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Size ${item.size}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    // Quantity selector
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
                        IconButton(
                            onClick = { onDecreaseQuantity(item.id) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_search),
                                contentDescription = "Decrease",
                                modifier = Modifier.size(16.dp),
                                tint = Color.Gray
                            )
                        }

                        Text(
                            text = item.quantity.toString(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        IconButton(
                            onClick = { onIncreaseQuantity(item.id) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_add),
                                contentDescription = "Increase",
                                modifier = Modifier.size(16.dp),
                                tint = Color.Gray
                            )
                        }
                    }
                }

                // Price
                Text(
                    text = "$${item.price}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        // Action buttons (Wishlist, Delete)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(
                onClick = { onAddToWishlist(item.id) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_edit),
                    contentDescription = "Add to wishlist",
                    tint = Color.Gray
                )
            }

            IconButton(
                onClick = { onRemove(item.id) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_delete),
                    contentDescription = "Delete",
                    tint = Color.Gray
                )
            }
        }
    }
}

