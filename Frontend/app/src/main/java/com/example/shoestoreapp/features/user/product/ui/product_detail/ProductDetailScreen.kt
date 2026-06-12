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
import androidx.compose.ui.unit.dp
import com.example.shoestoreapp.features.user.product.ui.components.*
import com.example.shoestoreapp.features.user.product.viewmodel.AddToCartUiState
import com.example.shoestoreapp.features.user.product.viewmodel.ProductDetailViewModel
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ProductDetailScreen(
    productGuid: String,
    passedColorName: String? = null,
    viewModel: ProductDetailViewModel = ProductDetailViewModel(),
    onBackClick: () -> Unit = {},
    onNavigateToCart: () -> Unit = {}
) {
    val productDetail by viewModel.productDetail.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val selectedColor by viewModel.selectedColor.collectAsState()
    val selectedImageUrl by viewModel.selectedImageUrl.collectAsState()

    val addToCartState by viewModel.addToCartState.collectAsState()
    val isShippingExpanded by viewModel.isShippingExpanded.collectAsState()
    val isDescriptionExpanded by viewModel.isDescriptionExpanded.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val isAddToCartLoading = addToCartState is AddToCartUiState.Loading

    var showAddToBagSheet by rememberSaveable { mutableStateOf(false) }
    var selectedSize by rememberSaveable(productGuid) { mutableStateOf<Int?>(null) }
    var quantity by rememberSaveable(productGuid) { mutableIntStateOf(1) }

    val bannerMessage by viewModel.bannerMessage.collectAsState()
    val isBannerSuccess by viewModel.isBannerSuccess.collectAsState()
    val showBanner by viewModel.showBanner.collectAsState()

    LaunchedEffect(productGuid) {
        viewModel.loadProductDetail(productGuid, passedColorName)
    }

    LaunchedEffect(addToCartState) {
        when (addToCartState) {
            is AddToCartUiState.Success -> {
                if (showAddToBagSheet) {
                    sheetState.hide()
                    showAddToBagSheet = false
                }
                kotlinx.coroutines.delay(1500)
                onNavigateToCart()
                viewModel.resetAddToCartState()
            }
            is AddToCartUiState.Error -> {
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
                imageUrl = selectedImageUrl ?: defaultVariant?.imageUrl,
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
                    price = selectedVariantForCart?.price ?: defaultVariant?.price ?: 0.0,
                    productType = productDetail?.categoryName ?: ""
                )

                Spacer(modifier = Modifier.height(32.dp))

                ActionButtonsSection(
                    onAddToCartClick = {
                        if (!productDetail?.variants.isNullOrEmpty()) {
                            showAddToBagSheet = true
                            coroutineScope.launch { sheetState.show() }
                        }
                    }
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
                    content = "Free standard shipping on orders over 50,000 ₫. Returns are accepted within 30 days of delivery for a full refund.",
                    isExpanded = isShippingExpanded,
                    onExpandedChange = { viewModel.toggleShippingExpanded() }
                )

                ExpandableSection(
                    title = "Product Description",
                    content = "Premium product from ${productDetail?.categoryName ?: "our collection"}. Crafted with quality materials and designed for comfort.",
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

        // ==========================================
        // BOTTOM SHEET CÓ CHỨA BANNER
        // ==========================================
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
                Box(modifier = Modifier.fillMaxWidth()) {
                    AddToBagBottomSheetContent(
                        productInfo = AddToBagProductInfo(
                            imageUrl = selectedImageUrl ?: defaultVariant?.imageUrl,
                            title = productDetail?.productName.orEmpty(),
                            category = productDetail?.categoryName ?: "Category",
                            price = selectedVariantForCart?.price ?: defaultVariant?.price ?: 0.0,
                            colorOptions = availableColors,
                            sizeOptions = availableSizes
                        ),
                        state = AddToBagSelectionState(
                            selectedColor = selectedColor,
                            selectedSize = selectedSize,
                            quantity = quantity,
                            isConfirmEnabled = selectedVariantForCart != null && !isAddToCartLoading,
                            isLoading = isAddToCartLoading
                        ),
                        actions = AddToBagActions(
                            onColorSelected = { color: String ->
                                val normalizedColor = color.trim()
                                viewModel.selectColor(normalizedColor)

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
                            onSizeSelected = { size: Int ->
                                selectedSize = size
                                viewModel.selectSize(size)
                            },
                            onDecreaseQuantity = {
                                if (quantity > 1) quantity -= 1
                            },
                            onIncreaseQuantity = {
                                quantity += 1
                            },
                            onClose = {
                                if (!isAddToCartLoading) {
                                    coroutineScope.launch {
                                        sheetState.hide()
                                        showAddToBagSheet = false
                                    }
                                }
                            },
                            onConfirm = {
                                selectedVariantForCart?.let { variant ->
                                    viewModel.addCartItem(variant.publicId, quantity)
                                }
                            }
                        )
                    )

                    // TopBanner trong Bottom Sheet
                    Box(modifier = Modifier.align(Alignment.TopCenter)) {
                        TopBanner(
                            message = bannerMessage,
                            isSuccess = isBannerSuccess,
                            isVisible = showBanner,
                            onDismiss = { viewModel.hideBanner() }
                        )
                    }
                }
            }
        }

        // TopBanner bên ngoài này vẫn giữ để báo lỗi ở màn hình chính (khi không mở Bottom Sheet)
        Box(modifier = Modifier.align(Alignment.TopCenter)) {
            TopBanner(
                message = bannerMessage,
                isSuccess = isBannerSuccess,
                isVisible = showBanner && !showAddToBagSheet, // Chỉ hiện khi Bottom Sheet đang ĐÓNG
                onDismiss = { viewModel.hideBanner() }
            )
        }
    }
}