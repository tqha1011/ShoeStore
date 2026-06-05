package com.example.shoestoreapp.features.user.product.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * TopAppBar: Thanh ứng dụng phía trên cùng (sử dụng CenterAlignedTopAppBar)
 * @param onMenuClick - Callback khi click menu
 * @param onShoppingBagClick - Callback khi click shopping bag
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    onMenuClick: () -> Unit = {},
    onShoppingBagClick: () -> Unit = {}
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = buildAnnotatedString {
                    // Định dạng cho chữ "Kicks"
                    withStyle(
                        style = SpanStyle(
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold
                        )
                    ) {
                        append("Kicks")
                    }
                    // Định dạng cho chữ "Hub" - Màu đỏ cam, in nghiêng
                    withStyle(
                        style = SpanStyle(
                            color = Color(0xFFE53935),
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic
                        )
                    ) {
                        append("Hub")
                    }
                },
                fontSize = 28.sp,
                letterSpacing = 1.5.sp,
                fontFamily = FontFamily.Serif,
                style = TextStyle(
                    // Thêm bóng đổ nhẹ
                    shadow = Shadow(
                        color = Color.LightGray,
                        offset = Offset(2f, 2f),
                        blurRadius = 4f
                    )
                )
            )
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Menu",
                    tint = Color.Black
                )
            }
        },
        actions = {
            IconButton(onClick = onShoppingBagClick) {
                Icon(
                    imageVector = Icons.Filled.ShoppingBag,
                    contentDescription = "Shopping Bag",
                    tint = Color.Black
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White,
            scrolledContainerColor = Color.Unspecified,
            navigationIconContentColor = Color.Black,
            titleContentColor = Color.Black,
            actionIconContentColor = Color.Black
        ),
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(top = 12.dp)
    )
}