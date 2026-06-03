package com.example.shoestoreapp.features.user.profile.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shoestoreapp.features.admin.product.ui.components.PhotoActionBottomSheet
import com.example.shoestoreapp.features.user.product.ui.components.TopBanner
import com.example.shoestoreapp.features.user.profile.ui.components.DatePickerField
import com.example.shoestoreapp.features.user.profile.ui.components.EditableAvatar
import com.example.shoestoreapp.features.user.profile.ui.components.ProfileEditField
import com.example.shoestoreapp.features.user.profile.viewmodel.EditProfileViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: EditProfileViewModel = viewModel(),
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val email by viewModel.email.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var showPhotoSheet by remember { mutableStateOf(false) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let { viewModel.onAvatarSelected(it) }
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                pendingCameraUri?.let { viewModel.onAvatarSelected(it) }
            }
            pendingCameraUri = null
        }
    )

    // Handle navigation after success
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            kotlinx.coroutines.delay(1500)
            viewModel.hideBanner()
            onBackClick()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "EDIT PROFILE",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            letterSpacing = 1.sp,
                            color = Color.Black
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        TextButton(
                            onClick = { viewModel.onSaveClicked(context) },
                            enabled = !uiState.isLoading
                        ) {
                            Text(text = "SAVE")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5F5F5))
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                EditableAvatar(
                    avatarUri = uiState.avatarUri,
                    avatarUrl = uiState.currentAvatarUrl,
                    onEditClick = { showPhotoSheet = true }
                )

                Spacer(modifier = Modifier.height(8.dp))

                ProfileEditField(
                    value = uiState.userName,
                    onValueChange = viewModel::onUserNameChanged,
                    label = "Name",
                    modifier = Modifier.fillMaxWidth()
                )

                DatePickerField(
                    value = uiState.dateOfBirthUi,
                    label = "Date of Birth",
                    modifier = Modifier.fillMaxWidth(),
                    onDateSelected = viewModel::onDateSelected
                )

                ProfileEditField(
                    value = email,
                    onValueChange = {},
                    label = "Email",
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    readOnly = true
                )
            }
        }

        if (showPhotoSheet) {
            PhotoActionBottomSheet(
                onDismiss = { showPhotoSheet = false },
                onTakePhotoClick = {
                    val uri = createTempImageUri(context)
                    pendingCameraUri = uri
                    showPhotoSheet = false
                    cameraLauncher.launch(uri)
                },
                onChooseFromGalleryClick = {
                    showPhotoSheet = false
                    galleryLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            )
        }

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

private fun createTempImageUri(context: Context): Uri {
    val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }
    val tempFile = File.createTempFile("camera_", ".jpg", imagesDir)
    val authority = "${context.packageName}.provider"
    return FileProvider.getUriForFile(context, authority, tempFile)
}
