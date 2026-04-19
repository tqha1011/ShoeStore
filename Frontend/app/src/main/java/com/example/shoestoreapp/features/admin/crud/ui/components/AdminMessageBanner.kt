package com.example.shoestoreapp.features.admin.crud.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Message Display Component (Error/Success)
 */
@Composable
fun AdminMessageBanner(
    message: String,
    isError: Boolean = true,
    onDismiss: () -> Unit = {}
) {
    Snackbar(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        containerColor = if (isError) AdminCrudColors.errorContainer else Color(0xFFC6EDCC),
        contentColor = if (isError) AdminCrudColors.onErrorContainer else Color(0xFF1B5E20),
        action = {
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Dismiss",
                    tint = if (isError) AdminCrudColors.onErrorContainer else Color(0xFF1B5E20)
                )
            }
        }
    ) {
        Text(
            text = message,
            fontSize = 12.sp,
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }
}

