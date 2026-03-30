package com.example.shoestoreapp.features.admin.product.ui.components
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class AdminBottomNavTab {
    ADMIN, ORDERS, ANALYTICS, SETTINGS
}

/**
 * Bottom Navigation Bar cho admin.
 * 
 * Tabs:
 * - ADMIN (admin_panel_settings icon)
 * - ORDERS (receipt_long icon)
 * - ANALYTICS (analytics icon)
 * - SETTINGS (settings icon)
 * 
 * Tab hiện tại sẽ có:
 * - Text màu đen, bold
 * - Icon filled (FILL=1)
 * - Border-top đen
 * 
 * Tab chưa chọn sẽ có:
 * - Text màu xám
 * - Icon outlined
 * 
 * @param selectedTab - Tab hiện tại được chọn
 * @param onTabSelected - Callback khi user click vào tab
 */
@Composable
fun AdminBottomNavBar(
    selectedTab: AdminBottomNavTab = AdminBottomNavTab.ADMIN,
    onTabSelected: (AdminBottomNavTab) -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(Color.White)
            .border(width = 1.dp, color = Color(0xFFE8E8E8)),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AdminBottomNavTab.entries.forEach { tab ->
            val isSelected = selectedTab == tab
            Column(
                modifier = Modifier
                    .clickable { onTabSelected(tab) }
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Icon
                when (tab) {
                    AdminBottomNavTab.ADMIN -> {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Admin",
                            modifier = Modifier.padding(bottom = 2.dp),
                            tint = if (isSelected) Color.Black else Color(0xFFBBBBBB)
                        )
                    }
                    AdminBottomNavTab.ORDERS -> {
                        Icon(
                            imageVector = Icons.Outlined.Receipt,
                            contentDescription = "Orders",
                            modifier = Modifier.padding(bottom = 2.dp),
                            tint = if (isSelected) Color.Black else Color(0xFFBBBBBB)
                        )
                    }
                    AdminBottomNavTab.ANALYTICS -> {
                        Icon(
                            imageVector = Icons.Outlined.Analytics,
                            contentDescription = "Analytics",
                            modifier = Modifier.padding(bottom = 2.dp),
                            tint = if (isSelected) Color.Black else Color(0xFFBBBBBB)
                        )
                    }
                    AdminBottomNavTab.SETTINGS -> {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            modifier = Modifier.padding(bottom = 2.dp),
                            tint = if (isSelected) Color.Black else Color(0xFFBBBBBB)
                        )
                    }
                }
                
                // Text
                Text(
                    text = tab.name,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                    fontSize = 10.sp,
                    letterSpacing = 0.8.sp,
                    color = if (isSelected) Color.Black else Color(0xFFBBBBBB)
                )
            }
        }
    }
}
