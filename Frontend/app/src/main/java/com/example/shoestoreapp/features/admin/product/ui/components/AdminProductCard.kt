package com.example.shoestoreapp.features.admin.product.ui.components
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import coil.compose.AsyncImage
import com.example.shoestoreapp.features.admin.product.data.models.AdminProduct
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
    onProductClick: (Int) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
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
            // Tên sản phẩm
            product.name?.let {
                Text(
                    text = it,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.Black,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            // Giá tiền
            Text(
                text = String.format(Locale.US, "$%.0f", product.price),
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = Color(0xFF999999)
            )
            // Stock
            Text(
                text = "Stock: ${product.stock}",
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = Color(0xFF666666),
                modifier = Modifier.padding(top = 4.dp)
            )
            // Variants count
            Text(
                text = "Variants: ${product.variantsCount}",
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = Color(0xFF666666)
            )
        }
    }
}
