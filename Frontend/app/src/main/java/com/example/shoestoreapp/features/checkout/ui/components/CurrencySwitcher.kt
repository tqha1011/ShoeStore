package com.example.shoestoreapp.features.checkout.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shoestoreapp.features.checkout.data.models.CurrencyType

/**
 * Component hiển thị Currency Switcher - cho phép người dùng chọn loại tiền tệ.
 *
 * @param selectedCurrency - Loại tiền tệ hiện tại
 * @param onCurrencySelected - Callback khi người dùng chọn currency
 *
 * UI Structure:
 * - Grid 3 cột: USD (selected), VND, EUR
 * - Selected currency: white background, black border, shadow
 * - Unselected currencies: hover effect, gray text
 */
@Composable
fun CurrencySwitcher(
    selectedCurrency: CurrencyType,
    onCurrencySelected: (CurrencyType) -> Unit,
    modifier: Modifier = Modifier
) {
    val currencies = CurrencyType.entries

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFF1F3FC),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            currencies.forEach { currency ->
                CurrencyButton(
                    currency = currency,
                    isSelected = currency == selectedCurrency,
                    onSelect = { onCurrencySelected(currency) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Component nút chọn currency đơn lẻ.
 *
 * @param currency - Loại tiền tệ
 * @param isSelected - Có phải currency được chọn hay không
 * @param onSelect - Callback khi click
 */
@Composable
fun CurrencyButton(
    currency: CurrencyType,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = if (isSelected) Color.White else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(enabled = !isSelected) { onSelect() }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = currency.code,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.Black else Color(0xFF999999),
                letterSpacing = 0.5.sp
            )
            Text(
                text = currency.symbol,
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                color = if (isSelected) Color.Black else Color(0xFFCCCCCC),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

