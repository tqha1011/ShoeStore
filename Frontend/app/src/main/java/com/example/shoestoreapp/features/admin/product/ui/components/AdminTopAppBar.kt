package com.example.shoestoreapp.features.admin.product.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun AdminTopAppBar(
    onAddProductClick: () -> Unit = {}
) {
    AdminManagementTopBar(
        title = "Product Management",
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
