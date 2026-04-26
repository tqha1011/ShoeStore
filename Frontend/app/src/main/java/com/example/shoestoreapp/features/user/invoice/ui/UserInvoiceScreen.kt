package com.example.shoestoreapp.features.user.invoice.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shoestoreapp.features.invoice.model.InvoiceStatus
import com.example.shoestoreapp.features.invoice.ui.components.InvoiceStatusFilterChipColors
import com.example.shoestoreapp.features.invoice.ui.components.InvoiceStatusFilterChipDimensions
import com.example.shoestoreapp.features.invoice.ui.components.InvoiceStatusFilterChipStyle
import com.example.shoestoreapp.features.invoice.ui.components.InvoiceStatusFilterChipTypography
import com.example.shoestoreapp.features.invoice.ui.components.InvoiceStatusFilterChips
import com.example.shoestoreapp.features.user.invoice.ui.components.UserOrderCard
import com.example.shoestoreapp.features.user.invoice.viewmodel.UserInvoiceViewModel
import com.example.shoestoreapp.features.user.product.ui.components.BottomNavBar
import com.example.shoestoreapp.features.user.product.ui.components.BottomNavTab

@Composable
fun UserInvoiceScreen(
    viewModel: UserInvoiceViewModel = UserInvoiceViewModel(),
    onTabSelected: (BottomNavTab) -> Unit = {}
) {

}

@Composable
private fun UserInvoiceFilterRow(
    selectedStatus: InvoiceStatus?,
    onFilterSelected: (InvoiceStatus?) -> Unit
) {
    InvoiceStatusFilterChips(
        selectedStatus = selectedStatus,
        onFilterSelected = onFilterSelected,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 2.dp),
        style = InvoiceStatusFilterChipStyle(
            dimensions = InvoiceStatusFilterChipDimensions(
                chipCornerRadius = 18.dp,
                chipHorizontalPadding = 12.dp,
                chipVerticalPadding = 7.dp
            ),
            colors = InvoiceStatusFilterChipColors(
                unselectedTextColor = Color(0xFF888888)
            ),
            typography = InvoiceStatusFilterChipTypography(letterSpacing = 0.sp),
            showSelectedBorder = true
        )
    )
}
