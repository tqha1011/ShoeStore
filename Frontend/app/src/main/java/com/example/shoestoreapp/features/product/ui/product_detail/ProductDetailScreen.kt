package com.example.shoestoreapp.features.product.ui.product_detail

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
import com.example.shoestoreapp.features.product.ui.components.ActionButtonsSection
import com.example.shoestoreapp.features.product.ui.components.ExpandableSection
import com.example.shoestoreapp.features.product.ui.components.ProductHeaderInfo
import com.example.shoestoreapp.features.product.ui.components.ProductHeroImage
import com.example.shoestoreapp.features.product.ui.components.SizeSelector
import com.example.shoestoreapp.features.product.viewmodel.ProductDetailViewModel

/**
 * ProductDetailScreen: Màn hình hiển thị chi tiết sản phẩm
 *
 * Chức năng chính:
 * - Hiển thị ảnh sản phẩm full-screen
 * - Hiển thị tên, giá, đánh giá sản phẩm
 * - Cho phép user chọn kích thước
 * - Cho phép user thêm vào giỏ hàng
 * - Cho phép user đánh dấu yêu thích
 * - Hiển thị thông tin thêm (shipping, description)
 *
 * @param productId - ID của sản phẩm cần hiển thị
 * @param viewModel - ProductDetailViewModel quản lý logic
 * @param onBackClick - Callback khi user click nút back (quay lại màn hình trước)
 * @param onNavigateToCart - Callback khi user thêm vào giỏ hàng (điều hướng sang cart screen)
 */
@Composable
fun ProductDetailScreen(
    productId: Int,
    viewModel: ProductDetailViewModel = ProductDetailViewModel(),
    onBackClick: () -> Unit = {},
    onNavigateToCart: () -> Unit = {}
) {
    // ============ STATE COLLECTIONS ============
    /**
     * Collect state từ ViewModel:
     * - productDetail: Thông tin sản phẩm hiện tại
     * - selectedSize: Size được chọn
     * - isLoading: Trạng thái đang tải
     */
    val productDetail by viewModel.productDetail.collectAsState(initial = null)
    val selectedSize by viewModel.selectedSize.collectAsState(initial = null)
    val isLoading by viewModel.isLoading.collectAsState(initial = false)

    // ============ LOAD DATA KHI COMPONENT KHỞI TẠO ============
    /**
     * LaunchedEffect: Chạy code một lần khi component khởi tạo
     * - key = productId: Nếu productId thay đổi, effect sẽ chạy lại
     * - Gọi loadProductDetail để tải dữ liệu sản phẩm
     */
    LaunchedEffect(productId) {
        println("ProductDetailScreen - LaunchedEffect triggered with productId: $productId")
        viewModel.loadProductDetail(productId)
    }

    // ============ LOADING STATE ============
    /**
     * Nếu isLoading = true hoặc productDetail = null:
     * - Hiển thị loading indicator ở giữa màn hình
     */
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

    // ============ MAIN CONTENT ============
    /**
     * Main layout: Column với scroll state
     * - Box fixed top: TopAppBar
     * - Column scrollable: Content chính
     */
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Scrollable Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ============ TOP APP BAR ============
            /**
             * Header bar chứa:
             * - Nút back (quay lại)
             * - Logo NIKE
             * - Icon shopping bag
             */
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Nút back
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

                // Logo NIKE
                Text(
                    text = "NIKE",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Black
                )

                // Icon shopping bag
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

            // ============ HERO IMAGE ============
            /**
             * Ảnh sản phẩm full-width, chiếm 1:1 aspect ratio
             */
            ProductHeroImage(
                imageUrl = productDetail?.imageUrl,
                contentDescription = productDetail?.name
            )

            // ============ CONTENT CONTAINER ============
            /**
             * Container chứa tất cả thông tin bên dưới ảnh
             * - Header Info (tên, giá, rating)
             * - Size Selector
             * - Action Buttons
             * - Expandable Sections (Shipping, Description)
             */
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp)
            ) {
                // ============ HEADER INFO ============
                ProductHeaderInfo(
                    name = productDetail?.name ?: "",
                    price = productDetail?.price ?: 0.0,
                    rating = productDetail?.rating ?: 0.0,
                    reviewCount = productDetail?.reviewCount ?: 0,
                    productType = productDetail?.productType ?: ""
                )

                // Spacer
                Spacer(modifier = Modifier.height(32.dp))

                // ============ SIZE SELECTOR ============
                SizeSelector(
                    selectedSize = selectedSize,
                    onSizeSelected = { size ->
                        viewModel.selectSize(size)
                    },
                    onSizeGuideClick = {
                        // TODO: Điều hướng sang Size Guide screen
                    }
                )

                // Spacer
                Spacer(modifier = Modifier.height(32.dp))

                // ============ ACTION BUTTONS ============
                ActionButtonsSection(
                    onAddToCartClick = {
                        // Thêm vào giỏ hàng
                        productDetail?.id?.let { id ->
                            viewModel.addToCart(id)
                            onNavigateToCart()
                        }
                    },
                    onFavoriteClick = {
                        // Toggle favorite
                        productDetail?.id?.let { id ->
                            viewModel.toggleFavorite(id)
                        }
                    },
                    isFavorite = productDetail?.isFavorite ?: false
                )

                // Spacer
                Spacer(modifier = Modifier.height(32.dp))

                // ============ DIVIDER ============
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFFE0E0E0))
                )

                // Spacer
                Spacer(modifier = Modifier.height(32.dp))

                // ============ EXPANDABLE SECTIONS ============
                /**
                 * Hai phần thông tin mở rộng:
                 * 1. Shipping & Returns
                 * 2. Product Description
                 */
                ExpandableSection(
                    title = "Shipping & Returns",
                    content = "Free standard shipping on orders over " + "$" + "50. Returns are accepted within 30 days of delivery for a full refund."
                )

                ExpandableSection(
                    title = "Product Description",
                    content = productDetail?.description ?: "The Nike Air Max 270 was the first lifestyle Air Max from Nike, delivering style, comfort and a giant attitude. The design draws inspiration from Air Max icons, showcasing Nike's greatest innovation with its large window and fresh array of colors."
                )

                // Spacer thêm để khoảng cách khi scroll
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

/**
 * Preview Function - dùng để xem giao diện trong Android Studio
 * Chỉ chạy trong Debug mode, không được build vào APK release
 */
@Composable
fun PreviewProductDetailScreen() {
    ProductDetailScreen(
        productId = 1,
        onBackClick = {},
        onNavigateToCart = {}
    )
}