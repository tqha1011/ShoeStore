package com.example.shoestoreapp.features.user.product.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

/**
 * ProductHeroImage: Component hiển thị ảnh sản phẩm chiếm toàn bộ chiều rộng
 * @param imageUrl - URL của ảnh sản phẩm
 * @param contentDescription - Mô tả ảnh (cho accessibility)
 * @param modifier - Modifier để tùy chỉnh layout
 */
@Composable
fun ProductHeroImage(
    modifier: Modifier = Modifier,
    imageUrl: String?,
    contentDescription: String? = null
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = contentDescription,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop
        )
    }
}

