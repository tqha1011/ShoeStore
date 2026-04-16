package com.example.shoestoreapp.features.admin.crud.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shoestoreapp.features.admin.crud.ui.components.*
import com.example.shoestoreapp.features.admin.crud.viewmodel.ProductCrudViewModel

/**
 * Admin Product CRUD Screen
 * Supports both Add and Edit product operations
 */
@Composable
fun AdminProductCrudScreen(
    viewModel: ProductCrudViewModel,
    onBackClick: () -> Unit = {},
) {
    // Collect form state from ViewModel
    val sizesList by viewModel.sizesList.collectAsState()
    val colorsList by viewModel.colorsList.collectAsState()
    val categoriesList by viewModel.categoriesList.collectAsState()
    val productName by viewModel.productName.collectAsState()
    val categoryName by viewModel.selectedCategoryName.collectAsState()
    val sizeValue by viewModel.selectedSizeValue.collectAsState()
    val colorName by viewModel.selectedColorName.collectAsState()
    val price by viewModel.price.collectAsState()
    val productId by viewModel.productId.collectAsState()
    val stock by viewModel.stock.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Clear messages after 3 seconds
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearErrorMessage()
        }
    }

    Scaffold(
        topBar = {
            AdminCrudTopAppBar(onBackClick = onBackClick)
        },
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        containerColor = Color.White
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Main Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Column(modifier = Modifier.padding(bottom = 24.dp)) {
                    Text(
                        text = "ADD/EDIT PRODUCT",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = AdminCrudColors.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Manage your retail inventory listings",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = AdminCrudColors.onPrimaryFixedVariant
                    )
                }

                // Product Photography Section
                Column(modifier = Modifier.padding(bottom = 24.dp)) {
                    Text(
                        text = "PRODUCT PHOTOGRAPHY",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp,
                        color = AdminCrudColors.onPrimaryFixedVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    AdminImageUploadCard(
                        onImageClick = { /* Handle image selection */ }
                    )
                }

                // Primary Info Section
                Column(modifier = Modifier.padding(bottom = 24.dp)) {
                    // Product Name
                    AdminFormField(
                        label = "TÊN SẢN PHẨM",
                        value = productName,
                        onValueChange = { viewModel.onProductNameChange(it) },
                        placeholder = "Enter product name..."
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Shoe Type and Price Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            AdminShoeTypeDropdown(
                                selectedType = categoryName,
                                categories = categoriesList,
                                onTypeSelected = { id, name ->
                                    viewModel.onCategorySelected(id, name)
                                }
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            AdminFormField(
                                label = "GIÁ ($)",
                                value = price.toString(),
                                onValueChange = { viewModel.onPriceChange(it.toDoubleOrNull() ?: 0.0) },
                                placeholder = "0.00",
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            AdminShoeSizeDropdown(
                                selectedSize = sizeValue,
                                sizesList = sizesList,
                                onSizeSelected = { id, value ->
                                    viewModel.onSizeSelected(id, value)
                                }
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            AdminShoeColorDropdown(
                                selectedColor = colorName,
                                colorsList = colorsList,
                                onColorSelected = { id, name ->
                                    viewModel.onColorSelected(id, name)
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // SKU and Stock Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            AdminFormField(
                                label = "SKU",
                                value = productId ?: " ",
                                onValueChange = { viewModel.onProductIdChange(it) },
                                placeholder = "NK-XXXX-XXXX"
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            AdminFormField(
                                label = "SỐ LƯỢNG KHO",
                                value = stock.toString(),
                                onValueChange = { viewModel.onStockChange(it.toIntOrNull() ?: 0) },
                                placeholder = "0",
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                            )
                        }
                    }
                }

                // Action Buttons
                AdminActionButtons(
                    onSaveClick = { viewModel.onSaveProduct() },
                    onDeleteClick = { viewModel.onDeleteProduct() },
                    isLoading = isLoading,
                    isSavingEnabled = !isLoading
                )

                Spacer(modifier = Modifier.height(100.dp))
            }

            // Error/Success Messages
            errorMessage?.let { errorMsg ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(androidx.compose.ui.Alignment.TopCenter)
                        .padding(top = 16.dp)
                ) {
                    AdminMessageBanner(
                        message = errorMsg,
                        isError = true,
                        onDismiss = { viewModel.clearErrorMessage() }
                    )
                }
            }

            errorMessage?.let { successMsg ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(androidx.compose.ui.Alignment.TopCenter)
                        .padding(top = 16.dp)
                ) {
                    AdminMessageBanner(
                        message = successMsg,
                        isError = false,
                        onDismiss = { viewModel.clearErrorMessage() }
                    )
                }
            }
        }
    }
}
