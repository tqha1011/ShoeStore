package com.example.shoestoreapp.features.admin.addproduct.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import com.example.shoestoreapp.features.admin.product.ui.components.AdminManagementTopBar

@Composable
fun AdminCrudTopAppBar(
    onBackClick: () -> Unit
) {
    AdminManagementTopBar(
        title = "Add Product",
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = AdminCrudColors.primary
                )
            }
        }
    )
}
