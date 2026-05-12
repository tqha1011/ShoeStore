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
import com.example.shoestoreapp.features.admin.product.ui.components.EditProductTopBar
import com.example.shoestoreapp.features.admin.product.ui.components.PhotoActionBottomSheet
import com.example.shoestoreapp.features.admin.product.ui.components.ProductFormSection
import com.example.shoestoreapp.features.admin.product.ui.components.VariantActionBottomSheet
import com.example.shoestoreapp.features.admin.product.ui.components.VariantManagementSection
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
    val sizes by viewModel.sizes.collectAsState()
    val colors by viewModel.colors.collectAsState()
    val variantDraft by viewModel.variantDraft.collectAsState()
    val isAddVariantSheetVisible by viewModel.isAddVariantSheetVisible.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    var showBottomSheet by remember { mutableStateOf(false) }
    var showVariantPhotoSheet by remember { mutableStateOf(false) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var pendingVariantCameraUri by remember { mutableStateOf<Uri?>(null) }
    val showDeleteConfirmation by viewModel.showDeleteConfirmation.collectAsState()

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
                is AdminEditProductUiEvent.ShowError -> {
                    android.widget.Toast.makeText(context, event.message, android.widget.Toast.LENGTH_SHORT).show()
                }
                AdminEditProductUiEvent.UpdateSuccess -> {
                    android.widget.Toast.makeText(context, "Update product success", android.widget.Toast.LENGTH_SHORT).show()
                    onBackClick()
                }
                AdminEditProductUiEvent.DeleteSuccess -> {
                    android.widget.Toast.makeText(context, "Delete product success", android.widget.Toast.LENGTH_SHORT).show()
                    onBackClick()
                }
                AdminEditProductUiEvent.VariantCreateSuccess -> {
                    android.widget.Toast.makeText(context, "Create variant success", android.widget.Toast.LENGTH_SHORT).show()
                }
                AdminEditProductUiEvent.VariantUpdateSuccess -> {
                    android.widget.Toast.makeText(context, "Update variant success", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            EditProductTopBar(onBackClick = onBackClick)
        },
        bottomBar = {
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
                        onClick = { viewModel.onSaveClick(productId) },
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
                        onClick = viewModel::onDeleteClicked,
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

    if (showBottomSheet) {
        PhotoActionBottomSheet(
            onDismiss = { showBottomSheet = false },
            onTakePhotoClick = {
                val uri = createTempImageUri(context)
                pendingCameraUri = uri
                showBottomSheet = false
                cameraLauncher.launch(uri)
            },
            onChooseFromGalleryClick = {
                showBottomSheet = false
                galleryLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        )
    }

    if (isAddVariantSheetVisible) {
        VariantActionBottomSheet(
            state = variantDraft,
            sizes = sizes,
            colors = colors,
            onDismiss = viewModel::onDismissVariantSheet,
            onImageClick = { showVariantPhotoSheet = true },
            onSizeSelected = { viewModel.updateVariantDraft(size = it) },
            onColorSelected = { viewModel.updateVariantDraft(color = it) },
            onPriceChange = { viewModel.updateVariantDraft(price = it) },
            onStockChange = { viewModel.updateVariantDraft(stock = it) },
            onSaveClick = { viewModel.onSaveVariant(context, productId) }
        )
    }

    if (showVariantPhotoSheet) {
        PhotoActionBottomSheet(
            onDismiss = { showVariantPhotoSheet = false },
            onTakePhotoClick = {
                val uri = createTempImageUri(context)
                pendingVariantCameraUri = uri
                showVariantPhotoSheet = false
                variantCameraLauncher.launch(uri)
            },
            onChooseFromGalleryClick = {
                showVariantPhotoSheet = false
                variantGalleryLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        )
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissDeleteDialog,
            title = { Text(text = "Delete product") },
            text = {
                Text(text = "Are you sure you want to delete this product? This action cannot be undone.")
            },
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
}

private fun createTempImageUri(context: Context): Uri {
    val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }
    val tempFile = File.createTempFile("camera_", ".jpg", imagesDir)
    val authority = "${context.packageName}.provider"
    return FileProvider.getUriForFile(context, authority, tempFile)
}
