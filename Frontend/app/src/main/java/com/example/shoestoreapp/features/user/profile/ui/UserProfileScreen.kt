package com.example.shoestoreapp.features.user.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.shoestoreapp.features.common.ui.components.LogoutActionSection
import com.example.shoestoreapp.features.user.product.ui.components.BottomNavBar
import com.example.shoestoreapp.features.user.product.ui.components.BottomNavTab

@Composable
fun UserProfileScreen(
    onTabSelected: (BottomNavTab) -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    Scaffold(
        bottomBar = {
            BottomNavBar(
                selectedTab = BottomNavTab.PROFILE,
                onTabSelected = onTabSelected
            )
        }
    ) { paddingValues ->
        LogoutActionSection(
            title = "Profile",
            description = "Log out to switch account quickly.",
            onLogoutClick = onLogoutClick,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        )
    }
}
