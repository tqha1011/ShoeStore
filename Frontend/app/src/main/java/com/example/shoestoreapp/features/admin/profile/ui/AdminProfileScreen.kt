package com.example.shoestoreapp.features.admin.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shoestoreapp.features.admin.product.ui.components.AdminBottomNavBar
import com.example.shoestoreapp.features.admin.product.ui.components.AdminBottomNavTab
import com.example.shoestoreapp.features.admin.product.ui.components.AdminManagementTopBar
import com.example.shoestoreapp.features.admin.profile.viewmodel.AdminProfileViewModel
import com.example.shoestoreapp.features.user.profile.ui.components.LogoutButton
import com.example.shoestoreapp.features.user.profile.ui.components.ProfileHeader
import com.example.shoestoreapp.features.user.profile.ui.components.ProfileMenuItem
import com.example.shoestoreapp.features.user.profile.ui.components.ProfileSectionLabel

@Composable
fun AdminProfileScreen(
    viewModel: AdminProfileViewModel = AdminProfileViewModel(),
    onTabSelected: (AdminBottomNavTab) -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onChangePasswordClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    val profile by viewModel.profileState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchAdminProfile()
    }

    Scaffold(
        topBar = {
            AdminManagementTopBar(title = "Profile Management")
        },
        bottomBar = {
            AdminBottomNavBar(
                selectedTab = AdminBottomNavTab.PROFILE,
                onTabSelected = onTabSelected
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            profile?.let {
                ProfileHeader(profile = it)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProfileSectionLabel(text = "Personal Information")

                ProfileMenuItem(
                    icon = Icons.Outlined.Person,
                    label = "Edit Profile",
                    onClick = onEditProfileClick
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProfileSectionLabel(text = "Security")

                ProfileMenuItem(
                    icon = Icons.Outlined.Lock,
                    label = "Change Password",
                    onClick = onChangePasswordClick
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            LogoutButton(
                onClick = {
                    onLogoutClick()
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "VERSION 2.6.0 (2026)",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = Color(0xFFB0B0B0)
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

