package com.example.shoestoreapp.features.admin.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.shoestoreapp.features.admin.product.ui.components.AdminBottomNavBar
import com.example.shoestoreapp.features.admin.product.ui.components.AdminBottomNavTab
import com.example.shoestoreapp.features.common.ui.components.LogoutActionSection

@Composable
fun AdminSettingsScreen(
    onTabSelected: (AdminBottomNavTab) -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    Scaffold(
        bottomBar = {
            AdminBottomNavBar(
                selectedTab = AdminBottomNavTab.SETTINGS,
                onTabSelected = onTabSelected
            )
        }
    ) { paddingValues ->
        LogoutActionSection(
            title = "Settings",
            description = "Sign out to test login with another account.",
            onLogoutClick = onLogoutClick,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        )
    }
}
