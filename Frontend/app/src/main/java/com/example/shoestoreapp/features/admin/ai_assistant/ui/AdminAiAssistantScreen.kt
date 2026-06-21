package com.example.shoestoreapp.features.admin.ai_assistant.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shoestoreapp.features.admin.ai_assistant.viewmodel.AiStrategyViewmodel
import com.example.shoestoreapp.features.admin.product.ui.components.AdminBottomNavBar
import com.example.shoestoreapp.features.admin.product.ui.components.AdminBottomNavTab
import com.example.shoestoreapp.features.agent_intelligent.product_assistant.ui.AiProductScreen
import com.example.shoestoreapp.features.agent_intelligent.product_assistant.viewmodel.AiProductViewmodel

enum class AdminAiMode {
    ANALYTICS,
    INVENTORY
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AdminAiAssistantScreen(
    analyticsViewModel: AiStrategyViewmodel,
    productViewModel: AiProductViewmodel,
    initialMode: AdminAiMode = AdminAiMode.ANALYTICS,
    initialPrompt: String? = null,
    onTabSelected: (AdminBottomNavTab) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    var selectedMode by remember { mutableStateOf(initialMode) }
    val selector: @Composable () -> Unit = {
        AdminAiModeSelector(
            selectedMode = selectedMode,
            onModeSelected = { selectedMode = it }
        )
    }
    val bottomBar: @Composable () -> Unit = {
        AdminBottomNavBar(
            selectedTab = AdminBottomNavTab.AI,
            onTabSelected = onTabSelected
        )
    }

    when (selectedMode) {
        AdminAiMode.ANALYTICS -> {
            AiStrategyScreen(
                viewModel = analyticsViewModel,
                initialPrompt = initialPrompt,
                onBackClick = onBackClick,
                title = "Admin AI Assistant",
                headerContent = selector,
                bottomBarContent = bottomBar
            )
        }

        AdminAiMode.INVENTORY -> {
            AiProductScreen(
                viewModel = productViewModel,
                onBackClick = onBackClick,
                showAdminPanels = true,
                title = "Admin AI Assistant",
                aiRoleName = "AI Product",
                userRoleName = "Admin",
                headerContent = selector,
                adminBottomBarContent = bottomBar
            )
        }
    }
}

@Composable
private fun AdminAiModeSelector(
    selectedMode: AdminAiMode,
    onModeSelected: (AdminAiMode) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AdminAiModeChip(
            text = "Analytics",
            selected = selectedMode == AdminAiMode.ANALYTICS,
            modifier = Modifier.weight(1f),
            onClick = { onModeSelected(AdminAiMode.ANALYTICS) }
        )
        AdminAiModeChip(
            text = "Inventory",
            selected = selectedMode == AdminAiMode.INVENTORY,
            modifier = Modifier.weight(1f),
            onClick = { onModeSelected(AdminAiMode.INVENTORY) }
        )
    }
}

@Composable
private fun AdminAiModeChip(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Text(
        text = text,
        fontSize = 15.sp,
        fontWeight = FontWeight.Black,
        color = if (selected) Color.White else Color.Black,
        modifier = modifier
            .background(
                color = if (selected) Color.Black else Color.White,
                shape = RoundedCornerShape(14.dp)
            )
            .border(1.5.dp, Color.Black, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 16.dp)
    )
}
