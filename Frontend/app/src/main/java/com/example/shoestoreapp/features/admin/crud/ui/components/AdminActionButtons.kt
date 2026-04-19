package com.example.shoestoreapp.features.admin.crud.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

/**
 * Action Buttons Component (Save/Delete)
 */
@Composable
fun AdminActionButtons(
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    isLoading: Boolean = false,
    isSavingEnabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DeleteButton(
            modifier = Modifier.weight(1f),
            isLoading = isLoading,
            onDeleteClick = onDeleteClick
        )

        SaveButton(
            modifier = Modifier.weight(2f),
            isLoading = isLoading,
            isSavingEnabled = isSavingEnabled,
            onSaveClick = onSaveClick
        )
    }
}

@Composable
private fun DeleteButton(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    onDeleteClick: () -> Unit
) {
    Button(
        onClick = onDeleteClick,
        modifier = modifier
            .height(56.dp)
            .border(
                shape = RoundedCornerShape(8.dp),
                width = 1.5.dp,
                color = AdminCrudColors.error
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = AdminCrudColors.error
        ),
        enabled = !isLoading
    ) {
        ButtonContent(
            isLoading = isLoading,
            loadingColor = AdminCrudColors.error,
            text = "DELETE PRODUCT"
        )
    }
}

@Composable
private fun SaveButton(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    isSavingEnabled: Boolean,
    onSaveClick: () -> Unit
) {
    Button(
        onClick = onSaveClick,
        modifier = modifier.height(56.dp),
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
