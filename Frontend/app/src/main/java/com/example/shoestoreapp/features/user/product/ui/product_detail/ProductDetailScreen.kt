package com.example.shoestoreapp.features.user.product.ui.product_detail

import android.widget.Toast
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.shoestoreapp.features.user.product.ui.components.ActionButtonsSection
import com.example.shoestoreapp.features.user.product.ui.components.AddToBagBottomSheetContent
import com.example.shoestoreapp.features.user.product.ui.components.ExpandableSection
import com.example.shoestoreapp.features.user.product.ui.components.ProductDetailTopAppBar
import com.example.shoestoreapp.features.user.product.ui.components.ProductHeaderInfo
import com.example.shoestoreapp.features.user.product.ui.components.ProductHeroImage
import com.example.shoestoreapp.features.user.product.viewmodel.AddToCartUiState
import com.example.shoestoreapp.features.user.product.viewmodel.ProductDetailViewModel
import kotlinx.coroutines.launch

/**
 * ProductDetailScreen: Màn hình hiển thị chi tiết sản phẩm
 * @param productGuid - GUID của sản phẩm cần hiển thị (String, không phải Int)
 * @param viewModel - ProductDetailViewModel quản lý logic
 * @param onBackClick - Callback khi user click nút back
 * @param onNavigateToCart - Callback khi user thêm vào giỏ hàng
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ProductDetailScreen(
    productGuid: String,
    viewModel: ProductDetailViewModel = ProductDetailViewModel(),
    onBackClick: () -> Unit = {},
    onNavigateToCart: () -> Unit = {}
) {
    val productDetail by viewModel.productDetail.collectAsState(initial = null)
    val isLoading by viewModel.isLoading.collectAsState(initial = false)
    val addToCartState by viewModel.addToCartState.collectAsState(initial = AddToCartUiState.Idle)
    val isShippingExpanded by viewModel.isShippingExpanded.collectAsState(initial = false)
    val isDescriptionExpanded by viewModel.isDescriptionExpanded.collectAsState(initial = false)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val isAddToCartLoading = addToCartState is AddToCartUiState.Loading

    var showAddToBagSheet by rememberSaveable { mutableStateOf(false) }
    var selectedSize by rememberSaveable(productGuid) { mutableStateOf<Int?>(null) }
    var selectedColor by rememberSaveable(productGuid) { mutableStateOf<String?>(null) }
    var quantity by rememberSaveable(productGuid) { mutableIntStateOf(1) }

    val context = LocalContext.current

    LaunchedEffect(productGuid) {
        viewModel.loadProductDetail(productGuid)
    }

    LaunchedEffect(addToCartState) {
        when (val state = addToCartState) {
            is AddToCartUiState.Success -> {
                // 1. Ẩn Bottom Sheet NGAY LẬP TỨC
                if (showAddToBagSheet) {
                    sheetState.hide()
                    showAddToBagSheet = false
                }
                // 2. Bắn Toast thông báo (hiển thị xuyên màn hình)
                Toast.makeText(context, "Added to cart successfully", Toast.LENGTH_SHORT).show()

                // 3. Chuyển trang sang Cart
                onNavigateToCart()

                // 4. Reset state
                viewModel.resetAddToCartState()
            }

            is AddToCartUiState.Error -> {
                // Lỗi thì khoan đóng Sheet, báo Toast cho user biết bị gì
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                viewModel.resetAddToCartState()
            }

            else -> Unit
        }
    }

    val selectableVariants = remember(productDetail) {
        productDetail?.variants
            ?.filter { variant ->
                !variant.colorName.isNullOrBlank() &&
                    variant.size != null &&
                    variant.isSelling &&
                    !variant.isDelete &&
                    variant.stock > 0
            }
            .orEmpty()
    }

    val availableColors = remember(selectableVariants) {
        selectableVariants
            .mapNotNull { it.colorName?.trim()?.takeIf(String::isNotBlank) }
            .distinctBy { it.lowercase() }
    }

    val availableSizes = remember(selectableVariants, selectedColor) {
        val normalizedSelectedColor = selectedColor?.trim()?.lowercase()
        selectableVariants
            .asSequence()
            .filter { variant ->
                normalizedSelectedColor == null ||
                    variant.colorName?.trim()?.lowercase() == normalizedSelectedColor
            }
            .mapNotNull { it.size }
            .distinct()
            .sorted()
            .toList()
    }

    val defaultVariant = remember(selectableVariants, productDetail) {
        selectableVariants.firstOrNull() ?: productDetail?.variants?.firstOrNull()
    }

    val selectedVariantForCart = remember(productDetail, selectedColor, selectedSize) {
        viewModel.findSelectedVariant(
            product = productDetail,
            selectedColor = selectedColor,
            selectedSize = selectedSize
        )
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
                onShoppingBagClick = { onNavigateToCart() }
            )

            ProductHeroImage(
                imageUrl = defaultVariant?.imageUrl,
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
                    price = defaultVariant?.price ?: 0.0,
                    rating = 0.0,
                    reviewCount = 0,
                    productType = productDetail?.brand ?: ""
                )

                Spacer(modifier = Modifier.height(32.dp))

                ActionButtonsSection(
                    onAddToCartClick = {
                        if (!productDetail?.variants.isNullOrEmpty()) {
                            showAddToBagSheet = true
                            coroutineScope.launch { sheetState.show() }
                        }
                    },
                    onFavoriteClick = {
                        productDetail?.publicId?.let {
                            // TODO: Implement favorite when backend endpoint is available.
                        }
                    },
                    isFavorite = false
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

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )

        if (showAddToBagSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    if (!isAddToCartLoading) {
                        showAddToBagSheet = false
                    }
                },
                sheetState = sheetState,
                containerColor = Color.White
            ) {
                AddToBagBottomSheetContent(
                    imageUrl = selectedVariantForCart?.imageUrl ?: defaultVariant?.imageUrl,
                    title = productDetail?.productName.orEmpty(),
                    category = "Men's Shoes",
                    price = selectedVariantForCart?.price ?: defaultVariant?.price ?: 0.0,
                    colorOptions = availableColors,
                    selectedColor = selectedColor,
                    onColorSelected = { color: String ->
                        val normalizedColor = color.trim()
                        selectedColor = normalizedColor

                        // Recompute sizes for the newly selected color to avoid stale-state mismatch.
                        val validSizesForColor = selectableVariants
                            .asSequence()
                            .filter { variant ->
                                variant.colorName?.trim().equals(normalizedColor, ignoreCase = true)
                            }
                            .mapNotNull { it.size }
                            .distinct()
                            .toList()

                        if (selectedSize !in validSizesForColor) {
                            selectedSize = null
                        }
                    },
                    sizeOptions = availableSizes,
                    selectedSize = selectedSize,
                    onSizeSelected = { size: Int ->
                        selectedSize = size
                        viewModel.selectSize(size)
                    },
                    quantity = quantity,
                    onDecreaseQuantity = {
                        if (quantity > 1) quantity -= 1
                    },
                    onIncreaseQuantity = {
                        quantity += 1
                    },
                    onSizeGuideClick = {
                        // TODO: Navigate to Size Guide screen.
                    },
                    onClose = {
                        if (!isAddToCartLoading) {
                            coroutineScope.launch {
                                sheetState.hide()
                                showAddToBagSheet = false
                            }
                        }
                    },
                    isLoading = isAddToCartLoading,
                    onConfirm = {
                        selectedVariantForCart?.let { variant ->
                            viewModel.addCartItem(variant.publicId, quantity)
                        }
                    },
                    isConfirmEnabled = selectedVariantForCart != null && !isAddToCartLoading
                )
            }
        }
    }
}
