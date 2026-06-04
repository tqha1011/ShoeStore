package com.example.shoestoreapp.features.admin.product.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.shoestoreapp.features.admin.product.viewmodel.AdminEditProductUiEvent
import com.example.shoestoreapp.features.admin.product.viewmodel.AdminEditProductViewModel
import com.example.shoestoreapp.features.admin.product.ui.components.*
import com.example.shoestoreapp.features.user.product.ui.components.TopBanner
import java.io.File

@Composable
fun AdminEditProductScreen(
    productId: String,
    viewModel: AdminEditProductViewModel = AdminEditProductViewModel(),
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val localImageUri by viewModel.localImageUri.collectAsState()
    val categories by viewModel.categories.collectAsState()

    val scrollState = rememberScrollState()
    val context = LocalContext.current

    var showBottomSheet by remember { mutableStateOf(false) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var pendingVariantCameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let { viewModel.uploadSelectedImage(context, it) }
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                pendingCameraUri?.let { viewModel.uploadSelectedImage(context, it) }
            }
        }
    )

    val variantGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let { viewModel.uploadVariantImage(context, it) }
        }
    )

    val variantCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                pendingVariantCameraUri?.let { viewModel.uploadVariantImage(context, it) }
            }
        }
    )

    LaunchedEffect(productId) {
        if (productId.isNotBlank()) {
            viewModel.loadProductDetails(productId)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is AdminEditProductUiEvent.DeleteSuccess -> {
                    kotlinx.coroutines.delay(1500)
                    onBackClick()
                }
                else -> Unit
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                EditProductTopBar(onBackClick = onBackClick)
            },
            bottomBar = {
                EditProductBottomBar(
                    onSaveClick = { viewModel.onSaveClick(productId) },
                    onDeleteClick = viewModel::onDeleteClicked
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 24.dp)
            ) {
                val imageModel = localImageUri ?: uiState.imageUrl
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(Color(0xFFF5F5F5))
                        .clickable { showBottomSheet = true },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageModel != null) {
                        AsyncImage(
                            model = imageModel,
                            contentDescription = "Product image",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = "Add photo",
                            tint = Color(0xFF9E9E9E),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                ProductFormSection(
                    name = uiState.productName,
                    brand = uiState.brand,
                    selectedCategory = uiState.selectedCategory,
                    categories = categories,
                    onNameChange = viewModel::onNameChange,
                    onBrandChange = viewModel::onBrandChange,
                    onCategoryChange = viewModel::onCategoryChange
                )
                Spacer(modifier = Modifier.height(24.dp))
                VariantManagementSection(
                    variants = uiState.variants,
                    onAddClick = viewModel::onAddVariantClick,
                    onEditClick = viewModel::onEditVariantClick,
                    onDeleteClick = viewModel::onDeleteVariantClick
                )
            }
        }

        MainPhotoBottomSheet(
            showBottomSheet = showBottomSheet,
            onDismiss = { showBottomSheet = false },
            context = context,
            onLaunchCamera = { uri ->
                pendingCameraUri = uri
                showBottomSheet = false
                cameraLauncher.launch(uri)
            },
            onLaunchGallery = {
                showBottomSheet = false
                galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        )

        VariantManagementOverlays(
            viewModel = viewModel,
            context = context,
            productId = productId,
            onLaunchCamera = { uri ->
                pendingVariantCameraUri = uri
                variantCameraLauncher.launch(uri)
            },
            onLaunchGallery = {
                variantGalleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        )

        DeleteConfirmations(
            viewModel = viewModel,
            productId = productId
        )

        // Banner thông báo
        Box(modifier = Modifier.align(Alignment.TopCenter)) {
            TopBanner(
                message = uiState.bannerMessage,
                isSuccess = uiState.isBannerSuccess,
                isVisible = uiState.showBanner,
                onDismiss = { viewModel.hideBanner() }
            )
        }
    }
}

@Composable
private fun EditProductBottomBar(
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Surface(
        shadowElevation = 6.dp,
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Button(
                onClick = onSaveClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "SAVE CHANGES",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onDeleteClick,
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, Color(0xFFB00020)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFB00020))
            ) {
                Text(
                    text = "DELETE PRODUCT",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun MainPhotoBottomSheet(
    showBottomSheet: Boolean,
    onDismiss: () -> Unit,
    context: Context,
    onLaunchCamera: (Uri) -> Unit,
    onLaunchGallery: () -> Unit
) {
    if (showBottomSheet) {
        PhotoActionBottomSheet(
            onDismiss = onDismiss,
            onTakePhotoClick = {
                val uri = createTempImageUri(context)
                onLaunchCamera(uri)
            },
            onChooseFromGalleryClick = onLaunchGallery
        )
    }
}

@Composable
private fun VariantManagementOverlays(
    viewModel: AdminEditProductViewModel,
    context: Context,
    productId: String,
    onLaunchCamera: (Uri) -> Unit,
    onLaunchGallery: () -> Unit
) {
    val isAddVariantSheetVisible by viewModel.isAddVariantSheetVisible.collectAsState()
    val variantDraft by viewModel.variantDraft.collectAsState()
    val sizes by viewModel.sizes.collectAsState()
    val colors by viewModel.colors.collectAsState()

    var showVariantPhotoSheet by remember { mutableStateOf(false) }

    if (isAddVariantSheetVisible) {
        VariantActionBottomSheet(
            state = variantDraft,
            sizes = sizes,
            colors = colors,
            actions = VariantBottomSheetActions(
                onDismiss = viewModel::onDismissVariantSheet,
                onImageClick = { showVariantPhotoSheet = true },
                onSizeSelected = { viewModel.updateVariantDraft(size = it) },
                onColorSelected = { viewModel.updateVariantDraft(color = it) },
                onPriceChange = { viewModel.updateVariantDraft(price = it) },
                onStockChange = { viewModel.updateVariantDraft(stock = it) },
                onSaveClick = { viewModel.onSaveVariant(context, productId) }
            )
        )
    }

    if (showVariantPhotoSheet) {
        PhotoActionBottomSheet(
            onDismiss = { },
            onTakePhotoClick = {
                val uri = createTempImageUri(context)
                onLaunchCamera(uri)
            },
            onChooseFromGalleryClick = {
                onLaunchGallery()
            }
        )
    }
}

@Composable
private fun DeleteConfirmations(
    viewModel: AdminEditProductViewModel,
    productId: String
) {
    val showDeleteConfirmation by viewModel.showDeleteConfirmation.collectAsState()
    val variantToDelete by viewModel.variantToDelete.collectAsState()

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissDeleteDialog,
            title = { Text(text = "Delete product") },
            text = { Text(text = "Are you sure you want to delete this product? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDelete(productId) }) {
                    Text(text = "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissDeleteDialog) {
                    Text(text = "Cancel")
                }
            }
        )
    }

    if (variantToDelete != null) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissDeleteVariantDialog,
            title = { Text(text = "Delete Variant?") },
            text = { Text(text = "Delete Variant? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDeleteVariant(productId) }) {
                    Text(text = "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissDeleteVariantDialog) {
                    Text(text = "Cancel")
                }
            }
        )
    }
}

private fun createTempImageUri(context: Context): Uri {
    val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }
    val tempFile = File.createTempFile("camera_", ".jpg", imagesDir)
    val authority = "${context.packageName}.provider"
    return FileProvider.getUriForFile(context, authority, tempFile)
}