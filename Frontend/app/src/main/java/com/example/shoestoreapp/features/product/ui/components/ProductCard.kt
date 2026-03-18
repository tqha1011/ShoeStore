package com.example.shoestoreapp.features.product.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.shoestoreapp.features.product.data.models.Product
import java.util.Locale

/**
 * ProductCard: Composable hiển thị card sản phẩm theo design mới
 * @param product: Sản phẩm cần hiển thị
 * @param onProductClick: Gọi callback khi click vào card
 * @param onFavoriteClick: Gọi callback khi click vào icon trái tim
 */
@Composable
fun ProductCard(
    product: Product,
    onProductClick: (Int) -> Unit = {},           // Click vào -> Navigate đến trang chi tiết
    onFavoriteClick: (Int) -> Unit = {}           // Click trái
) {
    // ============ CONTAINER CHÍNH (Product Card - Flex Column) ============
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // ========== PHẦN 1: ẢNH SẢN PHẨM + ICON TRÁI TIM (FAVORITE) ==========
        /**
         * Box với aspect ratio square để ảnh luôn là hình vuông
         * - Ảnh nằm phía sau làm background
         * - Favorite icon nằm ở góc trên phải
         * - Click vào Box -> navigate đến product detail screen
         */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)  // Tạo hình vuông
                .background(Color(0xFFF5F5F5))
                .clip(RoundedCornerShape(12.dp))
                .clickable { 
                    println("ProductCard clicked - Product ID: ${product.id}, Name: ${product.name}")
                    onProductClick(product.id)  // ← Click để navigate
                }
        ) {
            // Ảnh sản phẩm
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
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
                    .clickable { onFavoriteClick(product.id) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (product.isFavorite)
                        Icons.Filled.Favorite
                    else
                        Icons.Filled.FavoriteBorder,
                    contentDescription = "Favorite",
                    modifier = Modifier.size(20.dp),
                    tint = Color.Black
                )
            }
        }

        // ========== PHẦN 2: BADGE (VD: BESTSELLER, NEW RELEASE, etc.) ==========
        /**
         * Padding top 12.dp để cách biệt với ảnh
         */
        Box(
            modifier = Modifier
                .padding(top = 12.dp)
                .padding(horizontal = 4.dp)
        ) {
            Text(
                text = product.category,  // Dùng category từ model
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD2650F),  // Màu cam từ design
                letterSpacing = 0.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // ========== PHẦN 3: TÊN SẢN PHẨM ==========
        Text(
            text = product.name,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // ========== PHẦN 4: LOẠI GIÀY (VD: "Men's Shoes") ==========
        Text(
            text = product.productType,  // Dùng productType từ model
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // ========== PHẦN 5: GIÁ ==========
        Text(
            text = "$${String.format(Locale.US, "%.0f", product.price)}",  // Không hiển thị .00
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            color = Color.Black,
            modifier = Modifier.padding(top = 8.dp, start = 4.dp, end = 4.dp, bottom = 4.dp)
        )
    }
}


@Preview(showBackground = true)
@Composable
fun ProductCardPreview() {
    val sampleProduct = Product(
        id = 1,
        name = "Nike Air Max 270",
        imageUrl = "https://via.placeholder.com/300",
        description = "Red and Black",
        price = 150.0,
        rating = 4.5,
        reviewCount = 128,
        category = "BESTSELLER",
        productType = "Men's Shoes",
        isFavorite = false
    )

    ProductCard(
        product = sampleProduct,
        onProductClick = { productId -> println("Clicked product: $productId") },
        onFavoriteClick = { productId -> println("Favorited product: $productId") }
    )
}


