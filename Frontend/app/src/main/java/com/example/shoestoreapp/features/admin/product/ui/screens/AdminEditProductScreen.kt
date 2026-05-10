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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.shoestoreapp.features.admin.product.viewmodel.AdminEditProductViewModel
import com.example.shoestoreapp.features.admin.product.ui.components.EditProductTopBar
import com.example.shoestoreapp.features.admin.product.ui.components.PhotoActionBottomSheet
import com.example.shoestoreapp.features.admin.product.ui.components.ProductFormSection
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
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    var showBottomSheet by remember { mutableStateOf(false) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

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

    LaunchedEffect(productId) {
        if (productId.isNotBlank()) {
            viewModel.loadProductDetails(productId)
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
                        onClick = viewModel::onSaveClick,
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
                        onClick = viewModel::onDeleteClick,
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
                    androidx.compose.material3.Icon(
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
                category = uiState.category,
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
}

private fun createTempImageUri(context: Context): Uri {
    val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }
    val tempFile = File.createTempFile("camera_", ".jpg", imagesDir)
    val authority = "${context.packageName}.provider"
    return FileProvider.getUriForFile(context, authority, tempFile)
}
