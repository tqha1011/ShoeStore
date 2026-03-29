package com.example.shoestoreapp.features.user.product.ui.product_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shoestoreapp.features.user.product.ui.components.ActionButtonsSection
import com.example.shoestoreapp.features.user.product.ui.components.ExpandableSection
import com.example.shoestoreapp.features.user.product.ui.components.ProductHeaderInfo
import com.example.shoestoreapp.features.user.product.ui.components.ProductHeroImage
import com.example.shoestoreapp.features.user.product.ui.components.SizeSelector
import com.example.shoestoreapp.features.user.product.viewmodel.ProductDetailViewModel

/**
 * ProductDetailScreen: Màn hình hiển thị chi tiết sản phẩm
 * @param productId - ID của sản phẩm cần hiển thị
 * @param viewModel - ProductDetailViewModel quản lý logic
 * @param onBackClick - Callback khi user click nút back
 * @param onNavigateToCart - Callback khi user thêm vào giỏ hàng
 */
@Composable
fun ProductDetailScreen(
    productId: Int,
    viewModel: ProductDetailViewModel = ProductDetailViewModel(),
    onBackClick: () -> Unit = {},
    onNavigateToCart: () -> Unit = {}
) {
    val productDetail by viewModel.productDetail.collectAsState(initial = null)
    val selectedSize by viewModel.selectedSize.collectAsState(initial = null)
    val isLoading by viewModel.isLoading.collectAsState(initial = false)

    LaunchedEffect(productId) {
        println("🟡 ProductDetailScreen - LaunchedEffect triggered with productId: $productId")
        viewModel.loadProductDetail(productId)
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clickable { onBackClick() }
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black,
                        modifier = Modifier.width(24.dp)
                    )
                }

                Text(
                    text = "NIKE",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Black
                )

                Box(
                    modifier = Modifier
                        .clickable { }
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ShoppingBag,
                        contentDescription = "Shopping Bag",
                        tint = Color.Black,
                        modifier = Modifier.width(24.dp)
                    )
                }
            }

            ProductHeroImage(
                imageUrl = productDetail?.imageUrl,
                contentDescription = productDetail?.name
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp)
            ) {
                ProductHeaderInfo(
                    name = productDetail?.name ?: "",
                    price = productDetail?.price ?: 0.0,
                    rating = productDetail?.rating ?: 0.0,
                    reviewCount = productDetail?.reviewCount ?: 0,
                    productType = productDetail?.productType ?: ""
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
                        productDetail?.id?.let { id ->
                            viewModel.addToCart(id)
                            onNavigateToCart()
                        }
                    },
                    onFavoriteClick = {
                        productDetail?.id?.let { id ->
                            viewModel.toggleFavorite(id)
                        }
                    },
                    isFavorite = productDetail?.isFavorite ?: false
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
                    content = "Free standard shipping on orders over " + "$" + "50. Returns are accepted within 30 days of delivery for a full refund."
                )

                ExpandableSection(
                    title = "Product Description",
                    content = productDetail?.description ?: "The Nike Air Max 270 was the first lifestyle Air Max from Nike, delivering style, comfort and a giant attitude. The design draws inspiration from Air Max icons, showcasing Nike's greatest innovation with its large window and fresh array of colors."
                )

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

