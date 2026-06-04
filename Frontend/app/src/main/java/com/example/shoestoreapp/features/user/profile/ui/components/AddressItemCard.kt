package com.example.shoestoreapp.features.user.profile.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shoestoreapp.features.user.profile.data.models.AddressUiModel

@Composable
fun AddressItemCard(
    address: AddressUiModel,
    onEditClick: () -> Unit,
    onRemoveClick: () -> Unit,
    onSetDefaultClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (address.isDefault) Color(0xFFF8F8F8) else Color.White
    val borderColor = if (address.isDefault) Color(0xFFF3F4F6) else Color(0xFFE5E7EB)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(24.dp)) {

            if (address.isDefault) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .background(Color.Black, RoundedCornerShape(2.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "DEFAULT",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = address.fullAddress,
                    color = Color.Black,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    // Tránh việc text đè lên nhãn DEFAULT ở card đầu tiên
                    modifier = Modifier.padding(end = if (address.isDefault) 80.dp else 0.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Đường kẻ ngang mờ
                HorizontalDivider(color = borderColor)

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    TextButton(
                        onClick = onEditClick,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(imageVector = Icons.Outlined.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp), tint = Color.Black)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("EDIT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black, letterSpacing = 1.sp)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    TextButton(
                        onClick = onRemoveClick,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(imageVector = Icons.Outlined.Delete, contentDescription = "Remove", modifier = Modifier.size(16.dp), tint = Color(0xFFBA1A1A))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("REMOVE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFBA1A1A), letterSpacing = 1.sp)
                    }

                    if (!address.isDefault) {
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(
                            onClick = onSetDefaultClick,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("SET DEFAULT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black, letterSpacing = 1.sp)
                        }
                    }
                }
            }
        }
    }
}