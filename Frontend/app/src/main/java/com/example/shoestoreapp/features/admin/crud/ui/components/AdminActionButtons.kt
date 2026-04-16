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
        // Delete Button
        Button(
            onClick = { if (!isLoading) onDeleteClick() },
            modifier = Modifier
                .weight(1f)
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
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = AdminCrudColors.error,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "DELETE PRODUCT",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // Save Button
        Button(
            onClick = { if (!isLoading && isSavingEnabled) onSaveClick() },
            modifier = Modifier
                .weight(2f)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AdminCrudColors.primary,
                contentColor = AdminCrudColors.onPrimary,
                disabledContainerColor = AdminCrudColors.gray300
            ),
            enabled = !isLoading && isSavingEnabled
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = AdminCrudColors.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "SAVE PRODUCT",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
