package com.example.shoestoreapp.features.product.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * BottomNavBar: Thanh điều hướng phía dưới
 *
 * @param selectedTab - Tab hiện tại được chọn
 * @param onTabSelected - Callback khi chọn tab khác
 */
@Composable
fun BottomNavBar(
    selectedTab: BottomNavTab = BottomNavTab.HOME,
    onTabSelected: (BottomNavTab) -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround, // Phân bố các item đều nhau với khoảng cách giữa chúng.
        verticalAlignment = Alignment.CenterVertically // Căn giữa theo chiều dọc
    ) {
        // Home Tab
        BottomNavItem(
            icon = Icons.Filled.Home,
            label = "Home",
            isSelected = selectedTab == BottomNavTab.HOME,
            onClick = { onTabSelected(BottomNavTab.HOME) }
        )

        // Shop Tab
        BottomNavItem(
            icon = Icons.Filled.Storefront,
            label = "Shop",
            isSelected = selectedTab == BottomNavTab.SHOP,
            onClick = { onTabSelected(BottomNavTab.SHOP) }
        )

        // Favorites Tab
        BottomNavItem(
            icon = Icons.Filled.Favorite,
            label = "Favorites",
            isSelected = selectedTab == BottomNavTab.FAVORITES,
            onClick = { onTabSelected(BottomNavTab.FAVORITES) }
        )

        // Bag Tab
        BottomNavItem(
            icon = Icons.Filled.Search,
            label = "Bag",
            isSelected = selectedTab == BottomNavTab.BAG,
            onClick = { onTabSelected(BottomNavTab.BAG) }
        )

        // Profile Tab
        BottomNavItem(
            icon = Icons.Filled.Person,
            label = "Profile",
            isSelected = selectedTab == BottomNavTab.PROFILE,
            onClick = { onTabSelected(BottomNavTab.PROFILE) }
        )
    }
}

/**
 * BottomNavItem: Item riêng lẻ trong bottom nav
 */
@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier
                .padding(bottom = 4.dp),
            tint = if (isSelected) Color.Black else Color(0xFFBFBFBF)
        )
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.Black else Color(0xFFBFBFBF),
            letterSpacing = 0.5.sp
        )
    }
}

enum class BottomNavTab {
    HOME,
    SHOP,
    FAVORITES,
    BAG,
    PROFILE
}


