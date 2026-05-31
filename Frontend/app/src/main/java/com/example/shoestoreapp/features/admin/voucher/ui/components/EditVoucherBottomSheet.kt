package com.example.shoestoreapp.features.admin.voucher.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shoestoreapp.features.admin.product.ui.components.AdminFormField
import com.example.shoestoreapp.features.admin.voucher.viewmodel.VoucherUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditVoucherBottomSheet(
    uiState: VoucherUiState,
    onDismiss: () -> Unit,
    onVoucherNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onTargetApplicationChange: (Int) -> Unit,
    onDiscountStyleChange: (Int) -> Unit,
    onReleaseTypeChange: (Int) -> Unit,
    onDiscountValueChange: (String) -> Unit,
    onMaxReductionChange: (String) -> Unit,
    onMinOrderChange: (String) -> Unit,
    onTotalQuantityChange: (String) -> Unit,
    onMaxUsagePerUserChange: (String) -> Unit,
    onValidFromChange: (String) -> Unit,
    onValidToChange: (String) -> Unit,
    onUpdateCampaign: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Update Voucher",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            AdminFormField(
                label = "Voucher Name",
                value = uiState.voucherName,
                onValueChange = onVoucherNameChange,
                placeholder = "",
                isEnabled = false
            )
            AdminFormField(
                label = "Description",
                value = uiState.description,
                onValueChange = onDescriptionChange,
                placeholder = "",
                singleLine = false,
                minLines = 3
            )
            Divider(color = Color(0xFFE3E3E3))
            VoucherConfigSection(
                uiState = uiState,
                onTargetApplicationChange = onTargetApplicationChange,
                onDiscountStyleChange = onDiscountStyleChange,
                onReleaseTypeChange = onReleaseTypeChange,
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
            Button(
                onClick = onUpdateCampaign,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Update Campaign",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
