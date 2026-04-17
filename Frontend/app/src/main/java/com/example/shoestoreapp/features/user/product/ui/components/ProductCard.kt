package com.example.shoestoreapp.features.user.product.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
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
import com.example.shoestoreapp.features.user.product.data.models.Product
import java.util.Locale

/**
 * ProductCard: Composable hiển thị card sản phẩm theo design mới
 * Match với Product model mới (publicId, productName, brand, variants)
 * 
 * @param product: Sản phẩm cần hiển thị
 * @param onProductClick: Gọi callback khi click vào card (nhận publicId: String)
 * @param onFavoriteClick: Gọi callback khi click vào icon trái tim (nhận publicId: String)
 */
@Composable
fun ProductCard(
    product: Product,
    onProductClick: (String) -> Unit = {},
    onFavoriteClick: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // ========== PHẦN 1: ẢNH SẢN PHẨM + ICON TRÁI TIM ==========
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(Color(0xFFF5F5F5))
                .clip(RoundedCornerShape(12.dp))
                .clickable {
                    println("ProductCard clicked - Product GUID: ${product.publicId}, Name: ${product.productName}")
                    onProductClick(product.publicId)  // ← Pass publicId (GUID)
                }
        ) {
            AsyncImage(
                model = product.variants.firstOrNull()?.imageUrl,  // ← Lấy từ first variant
                contentDescription = product.productName,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            // Icon trái tim (favorite) - góc trên phải
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(32.dp)
                    .background(Color.White, RoundedCornerShape(50))
                    .clickable { onFavoriteClick(product.publicId) },  // ← Pass publicId
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.FavoriteBorder,  // ← Backend không cung cấp favorite status, luôn show empty
                    contentDescription = "Favorite",
                    modifier = Modifier.size(20.dp),
                    tint = Color.Black
                )
            }
        }

        // ========== PHẦN 2: BADGE (Brand thay vì category) ==========
        Box(
            modifier = Modifier
                .padding(top = 12.dp)
                .padding(horizontal = 4.dp)
        ) {
            Text(
                text = product.brand,  // ← Dùng brand thay vì category
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD2650F),
                letterSpacing = 0.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // ========== PHẦN 3: TÊN SẢN PHẨM ==========
        Text(
            text = product.productName,  // ← productName thay vì name
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // ========== PHẦN 4: SIZE/COLOR INFO (từ first variant) ==========
        Text(
            text = product.variants.firstOrNull()?.let { variant ->
                "Size ${variant.size} - ${variant.colorName ?: "Color"}"
            } ?: "Select variant",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // ========== PHẦN 5: GIÁ (từ first variant) ==========
        Text(
            text = "$${String.format(Locale.US, "%.0f", product.variants.firstOrNull()?.price ?: 0.0)}",  // ← Từ variant.price
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            color = Color.Black,
            modifier = Modifier.padding(top = 8.dp, start = 4.dp, end = 4.dp, bottom = 4.dp)
        )
    }
}



