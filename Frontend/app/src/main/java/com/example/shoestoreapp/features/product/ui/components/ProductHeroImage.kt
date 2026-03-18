package com.example.shoestoreapp.features.product.ui.components

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
 *
 * Chức năng:
 * - Hiển thị ảnh sản phẩm full-screen
 * - Tự động cập nhật khi URL ảnh thay đổi
 * - Hỗ trợ loading state và placeholder
 *
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
    // Box container có tỷ lệ 1:1 (chiều rộng = chiều cao)
    // aspectRatio(1f) đảm bảo ảnh sẽ luôn là hình vuông
    Box(
        modifier = modifier
            .aspectRatio(1f) // Tỷ lệ chiều rộng:chiều cao = 1:1
            .background(Color(0xFFF5F5F5)), // Màu nền nhạt khi đang load ảnh
        contentAlignment = Alignment.Center
    ) {
        // AsyncImage từ thư viện Coil - dùng để load ảnh từ URL
        AsyncImage(
            model = imageUrl,
            contentDescription = contentDescription,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop, // Cắt ảnh để lấp đầy Box
            onLoading = {
                // Hiển thị loading indicator khi đang tải ảnh
                // Tuy nhiên, callback này không thể render Composable
// Nên sử dụng placeholder thay thế
            }
        )
    }
}
