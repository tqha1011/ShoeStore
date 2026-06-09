package com.example.shoestoreapp.features.admin.product.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun AdminTopAppBar(
    onAiAssistantClick: () -> Unit = {},
    onAddProductClick: () -> Unit = {}
) {
    AdminManagementTopBar(
        title = "Product Management",
        navigationIcon = {
            IconButton(onClick = onAiAssistantClick) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = "AI Product Assistant",
                    tint = Color.Black
                )
            }
        },
        actions = {
            IconButton(onClick = onAddProductClick) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Product",
                    tint = Color.Black
                )
            }
        }
    )
}
