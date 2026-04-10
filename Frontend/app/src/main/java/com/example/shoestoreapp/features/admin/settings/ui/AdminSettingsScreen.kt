package com.example.shoestoreapp.features.admin.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.shoestoreapp.features.admin.product.ui.components.AdminBottomNavBar
import com.example.shoestoreapp.features.admin.product.ui.components.AdminBottomNavTab

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Text(
                text = "Sign out to test login with another account.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF666666)
            )

            Button(
                onClick = onLogoutClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                Text(text = "Log out")
            }
        }
    }
}

