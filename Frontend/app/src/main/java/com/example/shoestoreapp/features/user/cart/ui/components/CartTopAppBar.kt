package com.example.shoestoreapp.features.user.cart.ui.components

import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import com.example.shoestoreapp.features.user.product.ui.components.UserTopBarTitle

/**
 * TopAppBar Component cho Cart Screen (sử dụng CenterAlignedTopAppBar)
 * Hiển thị tiêu đề "SHOPPING BAG" ở giữa và số lượng items
 *
 * @param itemCount - Số lượng items trong giỏ
 * @param onBackClick - Callback khi click nút quay lại
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartTopAppBar(
    itemCount: Int,
    onBackClick: () -> Unit = {}
) {
    CenterAlignedTopAppBar(
        title = {
            UserTopBarTitle(text = "SHOPPING BAG")
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }
        },
        actions = {
            Text(
                text = "$itemCount ITEMS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                modifier = Modifier.padding(end = 8.dp)
            )
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

