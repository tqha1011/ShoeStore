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
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shoestoreapp.features.admin.voucher.viewmodel.VoucherUiState

@Composable
fun VoucherFormCard(
    uiState: VoucherUiState,
    onVoucherNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onTargetApplicationChange: (Int) -> Unit,
    onDiscountStyleChange: (Int) -> Unit,
    onDiscountValueChange: (String) -> Unit,
    onMaxReductionChange: (String) -> Unit,
    onMinOrderChange: (String) -> Unit,
    onTotalQuantityChange: (String) -> Unit,
    onMaxUsagePerUserChange: (String) -> Unit,
    onValidFromChange: (String) -> Unit,
    onValidToChange: (String) -> Unit,
    onInitializeCampaign: () -> Unit
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
                onVoucherNameChange = onVoucherNameChange,
                onDescriptionChange = onDescriptionChange
            )
            Divider(color = Color(0xFFE3E3E3))
            VoucherConfigSection(
                uiState = uiState,
                onTargetApplicationChange = onTargetApplicationChange,
                onDiscountStyleChange = onDiscountStyleChange,
                onDiscountValueChange = onDiscountValueChange,
                onMaxReductionChange = onMaxReductionChange,
                onMinOrderChange = onMinOrderChange
            )
            Divider(color = Color(0xFFE3E3E3))
            VoucherConditionSection(
                totalQuantity = uiState.totalQuantity,
                maxUsagePerUser = uiState.maxUsagePerUser,
                onTotalQuantityChange = onTotalQuantityChange,
                onMaxUsagePerUserChange = onMaxUsagePerUserChange
            )
            VoucherDurationSection(
                validFrom = uiState.validFrom,
                validTo = uiState.validTo,
                onValidFromChange = onValidFromChange,
                onValidToChange = onValidToChange
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onInitializeCampaign,
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

