package com.example.shoestoreapp.features.admin.addproduct.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import com.example.shoestoreapp.features.admin.addproduct.viewmodel.AddProductUiState
import com.example.shoestoreapp.features.admin.addproduct.viewmodel.AdminAddProductViewModel
import com.example.shoestoreapp.features.admin.addproduct.viewmodel.FetchMasterDataViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.shoestoreapp.features.admin.addproduct.ui.components.*
import com.example.shoestoreapp.features.admin.product.ui.components.AdminFormField
import kotlinx.coroutines.delay

/**
 * Admin Product CRUD Screen
 * Supports both Add and Edit product operations
 */
@Composable
fun AdminProductCrudScreen(
    viewModel: FetchMasterDataViewModel,
    addProductViewModel: AdminAddProductViewModel,
    onBackClick: () -> Unit = {},
    navController: NavHostController,
) {
    // Collect form state from ViewModel
    val categoriesList by viewModel.categoriesList.collectAsState()
    val productName by viewModel.productName.collectAsState()
    val categoryName by viewModel.selectedCategoryName.collectAsState()

    val addUiState by addProductViewModel.uiState.collectAsState()
    val isLoading = addUiState is AddProductUiState.Loading
    val errorMessage = (addUiState as? AddProductUiState.Error)?.message
    val successMessage = if (addUiState is AddProductUiState.Success) {
        "Product created successfully."
    } else {
        null
    }
    val isActionSuccess = addUiState is AddProductUiState.Success

    LaunchedEffect(isActionSuccess) {
        if (isActionSuccess) {
            navController.previousBackStackEntry?.savedStateHandle?.set("refresh_list", true)
            delay(3000)
            addProductViewModel.resetState()
            navController.popBackStack()
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

                // Product Name Field
                AdminFormField(
                    label = "PRODUCT NAME",
                    value = productName,
                    onValueChange = { viewModel.onProductNameChange(it) },
                    placeholder = "Enter product name"
                )

                // Category Dropdown - Same size as Product Name
                var selectedCategoryId by remember { mutableStateOf("") }

                AdminShoeTypeDropdown(
                    selectedType = categoryName,
                    categories = categoriesList,
                    onTypeSelected = { id, name ->
                        selectedCategoryId = id
                        viewModel.onCategorySelected(id, name)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Action Buttons - Full width (size of 2 fields above)
                AdminSaveButton(
                    onSaveClick = {
                        val categoryId = selectedCategoryId.toIntOrNull() ?: 0
                        addProductViewModel.saveProduct(productName = productName, categoryId = categoryId)
                    },
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
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                ) {
                    AdminMessageBanner(
                        message = errorMsg,
                        isError = true,
                        onDismiss = { addProductViewModel.resetState() }
                    )
                }
            }

            successMessage?.let { successMsg ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                ) {
                    AdminMessageBanner(
                        message = successMsg,
                        isError = false,
                        onDismiss = { addProductViewModel.resetState() }
                    )
                }
            }
        }
    }
}
