package com.example.shoestoreapp.features.user.product.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.shoestoreapp.R
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToLong

// 1. Gom nhóm thông tin sản phẩm
data class AddToBagProductInfo(
    val imageUrl: String?,
    val title: String,
    val category: String,
    val price: Double,
    val colorOptions: List<String>,
    val sizeOptions: List<Int>
)

// 2. Gom nhóm các trạng thái lựa chọn
data class AddToBagSelectionState(
    val selectedColor: String?,
    val selectedSize: Int?,
    val quantity: Int,
    val isConfirmEnabled: Boolean,
    val isLoading: Boolean = false
)

// 3. Gom nhóm các hành động (callbacks)
data class AddToBagActions(
    val onColorSelected: (String) -> Unit,
    val onSizeSelected: (Int) -> Unit,
    val onDecreaseQuantity: () -> Unit,
    val onIncreaseQuantity: () -> Unit,
    val onClose: () -> Unit,
    val onConfirm: () -> Unit
)

@Composable
fun AddToBagBottomSheetContent(
    modifier: Modifier = Modifier,
    productInfo: AddToBagProductInfo,
    state: AddToBagSelectionState,
    actions: AddToBagActions
) {
    // 1. BIẾN QUẢN LÝ TRẠNG THÁI HIỂN THỊ CỦA DIALOG
    var showSizeGuideDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp)
            .fillMaxHeight(0.95f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(Color(0xFFF7F7F7), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFEAEAEA), RoundedCornerShape(12.dp))
            ) {
                AsyncImage(
                    model = productInfo.imageUrl,
                    contentDescription = productInfo.title,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = productInfo.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = productInfo.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF757575),
                    modifier = Modifier.padding(top = 2.dp)
                )
                Text(
                    text = formatVnd(productInfo.price),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            IconButton(onClick = actions.onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close"
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "COLOR",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
            Row(modifier = Modifier.padding(top = 12.dp)) {
                productInfo.colorOptions.forEach { colorName ->
                    val isSelected = state.selectedColor == colorName
                    val borderColor = if (isSelected) Color.Black else Color(0xFFE0E0E0)

                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(44.dp)
                            .border(
                                BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = borderColor
                                ),
                                shape = CircleShape
                            )
                            .clickable { actions.onColorSelected(colorName) }
                            .padding(4.dp)
                            .background(color = colorNameToSwatch(colorName), shape = CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SIZE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                TextButton(
                    onClick = {
                        showSizeGuideDialog = true
                    }
                ) {
                    Text(
                        text = "SIZE GUIDE",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF616161),
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                productInfo.sizeOptions.forEach { size ->
                    val isSelected = state.selectedSize == size
                    OutlinedButton(
                        onClick = { actions.onSizeSelected(size) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected) Color.Black else Color.White,
                            contentColor = if (isSelected) Color.White else Color.Black
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) Color.Black else Color(0xFFE0E0E0)
                        )
                    ) {
                        Text(
                            text = size.toString(),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "QUANTITY",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                    .height(48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = actions.onDecreaseQuantity) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Decrease quantity"
                    )
                }
                Text(
                    text = state.quantity.toString(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(40.dp),
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
                IconButton(onClick = actions.onIncreaseQuantity) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increase quantity"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = actions.onConfirm,
            enabled = state.isConfirmEnabled && !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White,
                disabledContainerColor = Color(0xFFBDBDBD)
            )
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.DarkGray,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PROCESSING...",
                    fontWeight = FontWeight.Black,
                    color = Color.DarkGray
                )
            } else {
                Text(
                    text = "CONFIRM ADD TO BAG",
                    fontWeight = FontWeight.Black
                )
            }
        }
    }

    // 2. KHAI BÁO DIALOG
    if (showSizeGuideDialog) {
        Dialog(
            onDismissRequest = { showSizeGuideDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth(0.95f)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header chứa nút đóng
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Shoe Size Guide",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                        IconButton(
                            onClick = { showSizeGuideDialog = false }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close"
                            )
                        }
                    }

                    Image(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.size_guide),
                        contentDescription = "Size Chart",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 24.dp),
                        contentScale = ContentScale.FillWidth
                    )
                }
            }
        }
    }
}

private fun colorNameToSwatch(colorName: String): Color {
    return when (colorName.lowercase()) {
        "black" -> Color.Black
        "white" -> Color.White
        "red" -> Color(0xFFD32F2F)
        "blue" -> Color(0xFF1976D2)
        "green" -> Color(0xFF388E3C)
        "yellow" -> Color(0xFFFBC02D)
        "gray", "grey" -> Color(0xFF9E9E9E)
        "brown" -> Color(0xFF6D4C41)
        else -> {
            val hash = colorName.hashCode()
            val r = 80 + (hash and 0x7F)
            val g = 80 + ((hash shr 8) and 0x7F)
            val b = 80 + ((hash shr 16) and 0x7F)
            Color(r, g, b)
        }
    }
}

private fun formatVnd(price: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"))
    return "${formatter.format(price.roundToLong())} ₫"
}