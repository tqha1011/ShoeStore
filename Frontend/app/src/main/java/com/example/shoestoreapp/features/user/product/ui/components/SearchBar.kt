package com.example.shoestoreapp.features.user.product.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * SearchBar: Ô tìm kiếm sản phẩm
 * @param searchText - Text hiện tại trong search box (state được lift từ parent)
 * @param onSearchChanged - Callback khi text thay đổi
 */
@Composable
fun SearchBar(
    searchText: String = "",
    onSearchChanged: (String) -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5), RoundedCornerShape(50.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (searchText.isEmpty()) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Search",
                modifier = Modifier.padding(end = 12.dp),
                tint = Color.Gray
            )
            Text(
                text = "Search for shoes",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(start = 36.dp)
            )
        }

        BasicTextField(
            value = searchText,
            onValueChange = {
                onSearchChanged(it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 36.dp),
            textStyle = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                color = Color.Black
            ),
            singleLine = true
        )
    }
}

