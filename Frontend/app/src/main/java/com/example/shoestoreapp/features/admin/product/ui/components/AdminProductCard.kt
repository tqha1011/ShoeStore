package com.example.shoestoreapp.features.admin.product.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import coil.compose.AsyncImage
import com.example.shoestoreapp.features.admin.product.data.models.AdminProduct
import java.text.NumberFormat
import java.util.Locale

/**
 * Component hiển thị thông tin sản phẩm dạng card cho admin.
 * 
 * Cấu trúc:
 * - Ảnh sản phẩm (aspect ratio 1:1, rounded corners 16.dp)
 * - Tên sản phẩm (font bold)
 * - Giá tiền (font medium, gray)
 * - Trạng thái stock (IN STOCK, LOW STOCK: 08, OUT OF STOCK)
 * 
 * @param product - Dữ liệu sản phẩm cần hiển thị
 * @param onProductClick - Callback khi user click vào card (dùng để navigate tới edit screen)
 */
@Composable
fun AdminProductCard(
    product: AdminProduct,
    onProductClick: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onProductClick(product.id) }
    ) {
        // Ảnh sản phẩm
        AsyncImage(
            model = product.imageUrl,
            contentDescription = product.name,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(Color(0xFFF5F5F5))
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )
        // Thông tin sản phẩm
        Column(
            modifier = Modifier.padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = NumberFormat.getNumberInstance(Locale("vi", "VN"))
                        .format(product.price.toLong()) + " ₫",
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280),
                    maxLines = 1
                )
            }
            Text(
                text = "Variants: ${product.variantsCount}",
                fontSize = 12.sp,
                color = Color(0xFF6B7280)
            )
            Text(
                text = "Total stock: ${product.stock}",
                fontSize = 12.sp,
                color = Color(0xFF6B7280)
            )
        }
    }
}
