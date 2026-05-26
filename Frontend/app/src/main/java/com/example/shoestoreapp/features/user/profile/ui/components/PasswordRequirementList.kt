package com.example.shoestoreapp.features.user.profile.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PasswordRequirementList(
    hasMinLength: Boolean,
    hasUpperCase: Boolean,
    hasDigit: Boolean
) {
    RequirementRow(
        text = "Minimum 8 characters",
        isMet = hasMinLength
    )

    RequirementRow(
        text = "At least 1 uppercase letter",
        isMet = hasUpperCase
    )

    RequirementRow(
        text = "At least 1 number",
        isMet = hasDigit
    )
}

@Composable
private fun RequirementRow(
    text: String,
    isMet: Boolean
) {
    val tint = if (isMet) Color(0xFF2E7D32) else Color(0xFF9E9E9E)
    val icon = if (isMet) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked

    Row(horizontalArrangement = Arrangement.Start) {
        Icon(imageVector = icon, contentDescription = null, tint = tint)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, color = tint)
    }
}
