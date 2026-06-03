package com.example.shoestoreapp.features.admin.product.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.shoestoreapp.features.admin.addproduct.data.remote.master_data.ColorDto
import com.example.shoestoreapp.features.admin.addproduct.data.remote.master_data.SizeDto
import com.example.shoestoreapp.features.admin.product.data.models.ShoeColor
import com.example.shoestoreapp.features.admin.product.data.models.ShoeSize
import com.example.shoestoreapp.features.admin.product.data.models.VariantUiState

data class VariantBottomSheetActions(
    val onDismiss: () -> Unit,
    val onImageClick: () -> Unit,
    val onSizeSelected: (ShoeSize) -> Unit,
    val onColorSelected: (ShoeColor) -> Unit,
    val onPriceChange: (String) -> Unit,
    val onStockChange: (String) -> Unit,
    val onSaveClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VariantActionBottomSheet(
    state: VariantUiState,
    sizes: List<ShoeSize>,
    colors: List<ShoeColor>,
    actions: VariantBottomSheetActions
) {
    val sizeDtos = sizes.map { SizeDto(id = it.id.toString(), sizeValue = it.value) }
    val colorDtos = colors.map { ColorDto(id = it.id.toString(), colorName = it.name) }

    ModalBottomSheet(onDismissRequest = actions.onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                    .clickable(onClick = actions.onImageClick),
                contentAlignment = Alignment.Center
            ) {
                val imageModel = state.imageUri ?: state.existingImageUrl
                if (imageModel != null) {
                    AsyncImage(
                        model = imageModel,
                        contentDescription = "Variant image",
                        modifier = Modifier.matchParentSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = "Add variant photo",
                        tint = Color(0xFF9E9E9E),
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    AdminShoeSizeDropdown(
                        selectedSize = state.selectedSize?.value.orEmpty(),
                        sizesList = sizeDtos,
                        onSizeSelected = { id, _ ->
                            sizes.firstOrNull { it.id.toString() == id }?.let(actions.onSizeSelected)
                        },
                        enabled = state.variantId == null
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    AdminShoeColorDropdown(
                        selectedColor = state.selectedColor?.name.orEmpty(),
                        colorsList = colorDtos,
                        onColorSelected = { id, _ ->
                            colors.firstOrNull { it.id.toString() == id }?.let(actions.onColorSelected)
                        },
                        enabled = state.variantId == null
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    AdminFormField(
                        label = "PRICE",
                        value = state.price,
                        onValueChange = actions.onPriceChange,
                        placeholder = "0",
                        keyboardType = KeyboardType.Number
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    AdminFormField(
                        label = "STOCK",
                        value = state.stock,
                        onValueChange = actions.onStockChange,
                        placeholder = "0",
                        keyboardType = KeyboardType.Number
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = actions.onSaveClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = if (state.variantId == null) "Add Variant" else "Save Variant",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}