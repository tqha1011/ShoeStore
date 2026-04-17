package com.example.shoestoreapp.features.user.product.ui.product_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.shoestoreapp.features.user.product.ui.components.ActionButtonsSection
import com.example.shoestoreapp.features.user.product.ui.components.ExpandableSection
import com.example.shoestoreapp.features.user.product.ui.components.ProductDetailTopAppBar
import com.example.shoestoreapp.features.user.product.ui.components.ProductHeaderInfo
import com.example.shoestoreapp.features.user.product.ui.components.ProductHeroImage
import com.example.shoestoreapp.features.user.product.ui.components.SizeSelector
import com.example.shoestoreapp.features.user.product.viewmodel.ProductDetailViewModel

/**
 * ProductDetailScreen: Màn hình hiển thị chi tiết sản phẩm
 * @param productGuid - GUID của sản phẩm cần hiển thị (String, không phải Int)
 * @param viewModel - ProductDetailViewModel quản lý logic
 * @param onBackClick - Callback khi user click nút back
 * @param onNavigateToCart - Callback khi user thêm vào giỏ hàng
 */
@Composable
fun ProductDetailScreen(
    productGuid: String,  // Changed from Int to String (GUID)
    viewModel: ProductDetailViewModel = ProductDetailViewModel(),
    onBackClick: () -> Unit = {},
    onNavigateToCart: () -> Unit = {}
) {
    val productDetail by viewModel.productDetail.collectAsState(initial = null)
    val selectedSize by viewModel.selectedSize.collectAsState(initial = null)
    val isLoading by viewModel.isLoading.collectAsState(initial = false)
    val isShippingExpanded by viewModel.isShippingExpanded.collectAsState(initial = false)
    val isDescriptionExpanded by viewModel.isDescriptionExpanded.collectAsState(initial = false)

    LaunchedEffect(productGuid) {
        viewModel.loadProductDetail(productGuid)  // Pass GUID string
    }

    if (isLoading || productDetail == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.width(50.dp),
                color = Color.Black
            )
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            ProductDetailTopAppBar(
                onBackClick = onBackClick,
                onShoppingBagClick = {onNavigateToCart() }
            )

            ProductHeroImage(
                imageUrl = productDetail?.variants?.firstOrNull()?.imageUrl,
                contentDescription = productDetail?.productName,
                modifier = Modifier.padding(top = 12.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp)
            ) {
                ProductHeaderInfo(
                    name = productDetail?.productName ?: "",
                    price = productDetail?.variants?.firstOrNull()?.price ?: 0.0,
                    rating = 0.0,  // Backend không cung cấp rating
                    reviewCount = 0,  // Backend không cung cấp reviewCount
                    productType = productDetail?.brand ?: ""  // Dùng brand thay vì productType
                )

                Spacer(modifier = Modifier.height(32.dp))

                SizeSelector(
                    selectedSize = selectedSize,
                    onSizeSelected = { size ->
                        viewModel.selectSize(size)
                    },
                    onSizeGuideClick = {
                        // TODO: Navigate to Size Guide screen
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                ActionButtonsSection(
                    onAddToCartClick = {
                        productDetail?.publicId?.let { guid ->  // Use publicId instead of productGuid
                            viewModel.addToCart(guid, 1)
                            onNavigateToCart()
                        }
                    },
                    onFavoriteClick = {
                        productDetail?.publicId?.let {
                            // Favorite feature sẽ implement khi Backend có endpoint
                        }
                    },
                    isFavorite = false  // Backend không cung cấp favorite status
                )

                Spacer(modifier = Modifier.height(32.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFFE0E0E0))
                )

                Spacer(modifier = Modifier.height(32.dp))

                ExpandableSection(
                    title = "Shipping & Returns",
                    content = "Free standard shipping on orders over " + "$" + "50. Returns are accepted within 30 days of delivery for a full refund.",
                    isExpanded = isShippingExpanded,
                    onExpandedChange = { viewModel.toggleShippingExpanded() }
                )

                ExpandableSection(
                    title = "Product Description",
                    content = "Premium product from ${productDetail?.brand ?: "our collection"}. Crafted with quality materials and designed for comfort.",
                    isExpanded = isDescriptionExpanded,
                    onExpandedChange = { viewModel.toggleDescriptionExpanded() }
                )

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

