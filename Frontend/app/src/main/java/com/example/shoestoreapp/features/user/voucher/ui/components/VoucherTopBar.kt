package com.example.shoestoreapp.features.user.voucher.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.shoestoreapp.features.user.product.ui.components.UserTopBarTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoucherTopBar(
    title: String,
    onBackClick: (() -> Unit)? = null // Thêm callback để xử lý nút Back
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        CenterAlignedTopAppBar(
            title = {
                UserTopBarTitle(text = title.uppercase())
            },
            navigationIcon = {
                // Nếu màn hình nào truyền sự kiện onBackClick vào thì mới vẽ nút Back
                if (onBackClick != null) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White,
                scrolledContainerColor = Color.Unspecified,
                navigationIconContentColor = Color.Unspecified,
                titleContentColor = Color.Black,
                actionIconContentColor = Color.Unspecified
            )
        )
    }
}
