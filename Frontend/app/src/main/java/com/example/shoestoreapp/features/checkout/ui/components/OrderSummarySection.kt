package com.example.shoestoreapp.features.checkout.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shoestoreapp.features.checkout.data.models.CurrencyType
import com.example.shoestoreapp.features.checkout.data.models.OrderSummary

/**
 * Component hiển thị tóm tắt đơn hàng với phí vận chuyển, thuế và tổng tiền.
 *
 * @param orderSummary - Thông tin tóm tắt đơn hàng
 * @param selectedCurrency - Loại tiền tệ để định dạng giá
 *
 * UI Structure:
 * - Header: "ORDER SUMMARY"
 * - Product Item display (cố định: Nike Air Max Dn)
 * - Pricing Breakdown (Subtotal, Shipping, Tax, Total)
 */
@Composable
fun OrderSummarySection(
    orderSummary: OrderSummary,
    selectedCurrency: CurrencyType,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        // Header
        Text(
            text = "ORDER SUMMARY",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.2.sp,
            color = Color(0xFF181C22),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Order Summary Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color(0xFFF1F3FC),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(24.dp)
        ) {
            // Product Item
            ProductItemRow(
                productName = "Nike Air Max Dn",
                productType = "Men's Shoes | Black/White",
                quantity = 1,
                price = orderSummary.subtotal,
                currency = selectedCurrency
            )

            HorizontalDivider(
                color = Color(0xFFE0E0E0),
                thickness = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )

            // Pricing Breakdown
            PricingBreakdown(
                subtotal = orderSummary.subtotal,
                shipping = orderSummary.shipping,
                tax = orderSummary.tax,
                total = orderSummary.total,
                promoCode = orderSummary.promoCode,
                discountAmount = orderSummary.discountAmount,
                currency = selectedCurrency
            )
        }
    }
}

/**
 * Component hiển thị một sản phẩm trong đơn hàng.
 */
@Composable
fun ProductItemRow(
    productName: String,
    productType: String,
    quantity: Int,
    price: Double,
    currency: CurrencyType,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Product Name
        Text(
            text = productName,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        // Product Type
        Text(
            text = productType,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF999999),
            modifier = Modifier.padding(top = 4.dp)
        )

        // Quantity and Price
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "Qty $quantity",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF666666)
            )

            Text(
                text = formatPrice(price, currency),
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = Color.Black
            )
        }
    }
}

/**
 * Component hiển thị chi tiết phí vận chuyển, thuế và tổng tiền.
 */
@Composable
fun PricingBreakdown(
    modifier: Modifier = Modifier,
    subtotal: Double,
    shipping: Double,
    tax: Double,
    total: Double,
    promoCode: String = "",
    discountAmount: Double = 0.0,
    currency: CurrencyType
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Subtotal
        PricingRow(
            label = "Subtotal",
            value = formatPrice(subtotal, currency),
            isHighlight = false
        )

        // Shipping
        PricingRow(
            label = "Estimated Shipping",
            value = if (shipping == 0.0) "Free" else formatPrice(shipping, currency),
            isHighlight = false
        )

        // Tax
        PricingRow(
            label = "Tax",
            value = formatPrice(tax, currency),
            isHighlight = false
        )

        // Discount (if any)
        if (promoCode.isNotEmpty() && discountAmount > 0) {
            PricingRow(
                label = "Discount ($promoCode)",
                value = "- ${formatPrice(discountAmount, currency)}",
                isHighlight = false,
                valueColor = Color(0xFF27AE60)
            )
        }

        // Total
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TOTAL",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp,
                color = Color.Black
            )

            Text(
                text = formatPrice(total, currency),
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.3.sp,
                color = Color.Black
            )
        }
    }
}

/**
 * Component hiển thị một dòng trong phần chi tiết giá.
 */
@Composable
fun PricingRow(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    isHighlight: Boolean = false,
    valueColor: Color = Color.Black
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal,
            color = Color(0xFF666666)
        )

        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}

/**
 * Định dạng giá theo loại tiền tệ.
 */
fun formatPrice(price: Double, currency: CurrencyType): String {
    return when (currency.decimalPlaces) {
        0 -> "${price.toLong()}${currency.symbol}"
        else -> "${currency.symbol}${String.format("%.${currency.decimalPlaces}f", price)}"
    }
}

