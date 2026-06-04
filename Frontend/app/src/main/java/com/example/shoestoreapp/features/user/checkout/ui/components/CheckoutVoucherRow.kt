package com.example.shoestoreapp.features.user.checkout.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shoestoreapp.features.user.voucher.data.models.VoucherUiModel

@Composable
fun CheckoutVoucherRow(
    selectedProductVoucher: VoucherUiModel? = null,
    selectedShippingVoucher: VoucherUiModel? = null,
    onClick: () -> Unit
) {
    val hasVoucher = selectedProductVoucher != null || selectedShippingVoucher != null

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "SHOE STORE VOUCHER",
            fontSize = 11.sp,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, if (hasVoucher) Color.Black else Color(0xFFE5E7EB)),
            color = if (hasVoucher) Color(0xFFFAFAFA) else Color.White
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                VoucherDetails(
                    hasVoucher = hasVoucher,
                    productVoucher = selectedProductVoucher,
                    shippingVoucher = selectedShippingVoucher,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                    contentDescription = "Arrow Right",
                    tint = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun VoucherDetails(
    hasVoucher: Boolean,
    productVoucher: VoucherUiModel?,
    shippingVoucher: VoucherUiModel?,
    modifier: Modifier = Modifier
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Icon(
            imageVector = Icons.Outlined.ConfirmationNumber,
            contentDescription = "Voucher",
            tint = if (hasVoucher) Color.Black else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))

        if (!hasVoucher) {
            Text(
                text = "Select or enter code",
                color = Color.Gray,
                fontSize = 14.sp
            )
        } else {
            AppliedVouchersList(productVoucher, shippingVoucher)
        }
    }
}

@Composable
private fun AppliedVouchersList(
    productVoucher: VoucherUiModel?,
    shippingVoucher: VoucherUiModel?
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        productVoucher?.let {
            Text(
                text = "[Product] - ${it.discountValue}",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF16A34A),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        shippingVoucher?.let {
            Text(
                text = "[Shipping] - ${it.discountValue}",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF0284C7),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}