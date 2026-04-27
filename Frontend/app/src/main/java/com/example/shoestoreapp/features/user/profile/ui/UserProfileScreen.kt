package com.example.shoestoreapp.features.user.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.shoestoreapp.features.invoice.model.InvoiceStatus
import com.example.shoestoreapp.features.user.product.ui.components.BottomNavBar
import com.example.shoestoreapp.features.user.product.ui.components.BottomNavTab
import com.example.shoestoreapp.features.user.profile.ui.components.UserOrdersSection

@Composable
fun UserProfileScreen(
    onTabSelected: (BottomNavTab) -> Unit = {},
    onViewOrdersClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    orderCounts: Map<InvoiceStatus, Int> = emptyMap(),
    onStatusClick: (InvoiceStatus) -> Unit = { onViewOrdersClick() }
) {
    Scaffold(
        bottomBar = {
            BottomNavBar(
                selectedTab = BottomNavTab.PROFILE,
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Profile",
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Manage your account and orders.",
                color = Color(0xFF666666)
            )

            UserOrdersSection(
                orderCounts = orderCounts,
                onStatusClick = onStatusClick,
                onViewHistoryClick = onViewOrdersClick
            )

            Button(
                onClick = onLogoutClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                Text(text = "Log out")
            }
        }
    }
}
