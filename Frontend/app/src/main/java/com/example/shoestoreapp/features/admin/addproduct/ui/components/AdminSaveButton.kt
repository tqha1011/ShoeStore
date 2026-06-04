package com.example.shoestoreapp.features.admin.addproduct.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AdminSaveButton(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    isSavingEnabled: Boolean,
    onSaveClick: () -> Unit
) {
    Button(
        onClick = onSaveClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AdminCrudColors.primary,
            contentColor = AdminCrudColors.onPrimary,
            disabledContainerColor = AdminCrudColors.gray300
        ),
        enabled = !isLoading && isSavingEnabled
    ) {
        ButtonContent(
            isLoading = isLoading,
            loadingColor = AdminCrudColors.onPrimary,
            text = "SAVE PRODUCT"
        )
    }
}

@Composable
private fun ButtonContent(
    isLoading: Boolean,
    loadingColor: Color,
    text: String
) {
    if (isLoading) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            color = loadingColor,
            strokeWidth = 2.dp
        )
    } else {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}
