package com.example.shoestoreapp.features.admin.voucher.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shoestoreapp.features.admin.voucher.viewmodel.VoucherUiState

data class VoucherFormActions(
    val onVoucherNameChange: (String) -> Unit,
    val onDescriptionChange: (String) -> Unit,
    val onTargetApplicationChange: (Int) -> Unit,
    val onDiscountStyleChange: (Int) -> Unit,
    val onReleaseTypeChange: (Int) -> Unit,
    val onDiscountValueChange: (String) -> Unit,
    val onMaxReductionChange: (String) -> Unit,
    val onMinOrderChange: (String) -> Unit,
    val onTotalQuantityChange: (String) -> Unit,
    val onMaxUsagePerUserChange: (String) -> Unit,
    val onValidFromChange: (String) -> Unit,
    val onValidToChange: (String) -> Unit,
    val onInitializeCampaign: () -> Unit
)

@Composable
fun VoucherFormCard(
    uiState: VoucherUiState,
    actions: VoucherFormActions
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE3E3E3))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            VoucherBasicInfoSection(
                voucherName = uiState.voucherName,
                description = uiState.description,
                onVoucherNameChange = actions.onVoucherNameChange,
                onDescriptionChange = actions.onDescriptionChange
            )
            HorizontalDivider(Modifier, DividerDefaults.Thickness, color = Color(0xFFE3E3E3))
            VoucherConfigSection(
                uiState = uiState,
                onTargetApplicationChange = actions.onTargetApplicationChange,
                onDiscountStyleChange = actions.onDiscountStyleChange,
                onReleaseTypeChange = actions.onReleaseTypeChange,
                onDiscountValueChange = actions.onDiscountValueChange,
                onMaxReductionChange = actions.onMaxReductionChange,
                onMinOrderChange = actions.onMinOrderChange
            )
            HorizontalDivider(Modifier, DividerDefaults.Thickness, color = Color(0xFFE3E3E3))
            VoucherConditionSection(
                totalQuantity = uiState.totalQuantity,
                maxUsagePerUser = uiState.maxUsagePerUser,
                onTotalQuantityChange = actions.onTotalQuantityChange,
                onMaxUsagePerUserChange = actions.onMaxUsagePerUserChange
            )
            VoucherDurationSection(
                validFrom = uiState.validFrom,
                validTo = uiState.validTo,
                onValidFromChange = actions.onValidFromChange,
                onValidToChange = actions.onValidToChange
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = actions.onInitializeCampaign,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Initialize Campaign",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}