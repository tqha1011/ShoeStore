package com.example.shoestoreapp.features.admin.crud.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Image Upload Card Component
 */
@Composable
fun AdminImageUploadCard(
    onImageClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 10f)
            .background(
                color = AdminCrudColors.surfaceContainerLow,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 2.dp,
                color = AdminCrudColors.outlineVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onImageClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = Icons.Outlined.AddAPhoto,
                contentDescription = "Add Photo",
                tint = AdminCrudColors.outline,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Upload main product view",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = AdminCrudColors.outline
            )
        }
    }
}

