package com.example.shoestoreapp.features.user.product.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ActionButtonsSection: Component chứa các nút hành động (Add to Bag, Favorite)
 * @param onAddToCartClick - Callback khi user click nút "ADD TO BAG"
 * @param onFavoriteClick - Callback khi user click nút "FAVORITE"
 * @param isFavorite - Trạng thái hiện tại có phải yêu thích không
 * @param modifier - Modifier để tùy chỉnh layout
 */
@Composable
fun ActionButtonsSection(
    modifier: Modifier = Modifier,
    onAddToCartClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    isFavorite: Boolean = false
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Button(
            onClick = onAddToCartClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(100.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.elevatedButtonElevation(
                defaultElevation = 8.dp,
                pressedElevation = 4.dp
            )
        ) {
            Text(
                text = "ADD TO BAG",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .border(
                    width = 1.5.dp,
                    color = Color(0xFFE0E0E0),
                    shape = RoundedCornerShape(100.dp)
                )
                .clickable { onFavoriteClick() }
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = "Favorite",
                tint = if (isFavorite) Color.Red else Color.Black,
                modifier = Modifier.width(20.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "FAVORITE",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = Color.Black,
                letterSpacing = 0.5.sp
            )
        }
    }
}

