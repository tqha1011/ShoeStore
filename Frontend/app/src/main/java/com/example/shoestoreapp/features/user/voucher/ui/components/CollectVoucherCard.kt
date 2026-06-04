package com.example.shoestoreapp.features.user.voucher.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shoestoreapp.features.user.voucher.data.models.VoucherUiModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CollectVoucherCard(
    voucher: VoucherUiModel,
    modifier: Modifier = Modifier,
    onCollect: (voucherId: String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit
) {
    val initialState = if (voucher.isCollected) CollectState.Collected else CollectState.Idle
    var collectState by remember(voucher.isCollected) { mutableStateOf(initialState) }
    val coroutineScope = rememberCoroutineScope()

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFF1F1F1)),
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Surface(
                color = Color(0xFFF9F9FF),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(96.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = voucher.discountValue.uppercase(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = voucher.scope.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9CA3AF),
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = voucher.title.uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = voucher.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF9CA3AF)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "EXPIRES ${voucher.expiryDate.uppercase()}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9CA3AF),
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            CollectButton(
                state = collectState,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (collectState != CollectState.Idle) return@CollectButton
                    collectState = CollectState.Loading
                    onCollect(
                        voucher.id,
                        {
                            coroutineScope.launch {
                                collectState = CollectState.Success
                                delay(1000)
                                collectState = CollectState.Collected
                            }
                        },
                        {
                            collectState = CollectState.Idle
                            // Toast removed, handled by ViewModel banner state
                        }
                    )
                }
            )
        }
    }
}

private enum class CollectState {
    Idle,
    Loading,
    Success,
    Collected
}

@Composable
private fun CollectButton(
    state: CollectState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val buttonColor = when (state) {
        CollectState.Idle, CollectState.Loading -> Color.Black
        CollectState.Success -> Color(0xFF16A34A)
        CollectState.Collected -> Color(0xFFE5E7EB)
    }
    val contentColor = when (state) {
        CollectState.Collected -> Color(0xFF9CA3AF)
        else -> Color.White
    }
    val rotationTransition = rememberInfiniteTransition(label = "collect-rotation")
    val rotation by rotationTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing)
        ),
        label = "collect-rotation-value"
    )

    Button(
        onClick = onClick,
        enabled = state == CollectState.Idle,
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor,
            contentColor = contentColor,
            disabledContainerColor = buttonColor,
            disabledContentColor = contentColor
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 14.dp),
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(6.dp))
            .animateContentSize()
    ) {
        AnimatedContent(targetState = state, label = "collect-button") { targetState ->
            when (targetState) {
                CollectState.Idle -> {
                    Text(
                        text = "COLLECT",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
                CollectState.Loading -> {
                    Icon(
                        imageVector = Icons.Outlined.Sync,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(rotation)
                    )
                }
                CollectState.Success -> {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                CollectState.Collected -> {
                    Text(
                        text = "COLLECTED",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}
