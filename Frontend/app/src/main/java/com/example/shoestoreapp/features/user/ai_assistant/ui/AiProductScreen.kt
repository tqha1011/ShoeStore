package com.example.shoestoreapp.features.user.ai_assistant.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shoestoreapp.features.agent_intelligent.ui.BaseAIChatScreen
import com.example.shoestoreapp.features.user.ai_assistant.data.remote.AddVariantResultDto
import com.example.shoestoreapp.features.user.ai_assistant.data.remote.SearchProductResultDto
import com.example.shoestoreapp.features.user.ai_assistant.viewmodel.AiProductViewmodel
import com.example.shoestoreapp.features.user.product.data.remote.ProductResponseDto

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AiProductScreen(
    viewModel: AiProductViewmodel,
    initialPrompt: String? = null,
    onBackClick: () -> Unit = {}
) {
    val searchResult by viewModel.searchResultState.collectAsState()
    val variantDraft by viewModel.variantDraftState.collectAsState()

    val actionTextState = remember(variantDraft) {
        mutableStateOf(buildVariantActionText(variantDraft))
    }

    BaseAIChatScreen(
        viewModel = viewModel,
        title = "Product Assistant",
        aiRoleName = "AI ASSISTANT",
        userRoleName = "USER",
        initialPrompt = initialPrompt,
        onBackClick = onBackClick,
        headerContent = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                searchResult?.let { result ->
                    SearchResultPanel(
                        result = result,
                        onClear = { viewModel.clearSearchResult() },
                        onProductClick = { productId ->
                            viewModel.SendMessage("Select productId=$productId")
                            viewModel.clearSearchResult()
                        }
                    )
                }
                variantDraft?.let { draft ->
                    VariantDraftPanel(
                        draft = draft,
                        actionText = actionTextState.value,
                        onActionTextChange = { actionTextState.value = it },
                        onSubmit = { viewModel.submitVariantDraft(actionTextState.value) },
                        onDismiss = { viewModel.clearVariantDraft() }
                    )
                }
            }
        }
    )
}

@Composable
private fun SearchResultPanel(
    result: SearchProductResultDto,
    onClear: () -> Unit,
    onProductClick: (String) -> Unit
) {
    val products = result.products.orEmpty()

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Search Results",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onClear) {
                    Text(text = "Clear", fontSize = 12.sp)
                }
            }
            result.message?.let {
                Text(text = it, fontSize = 12.sp, color = Color(0xFF666666))
            }
            if (products.isEmpty()) {
                Text(text = "No products found.", fontSize = 12.sp, color = Color(0xFF666666))
            } else {
                products.take(5).forEach { product ->
                    ProductSearchRow(
                        product = product,
                        onClick = { onProductClick(product.publicId) }
                    )
                }
                if (products.size > 5) {
                    Text(
                        text = "Showing ${products.take(5).size} of ${products.size} products",
                        fontSize = 11.sp,
                        color = Color(0xFF888888)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductSearchRow(product: ProductResponseDto, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, MaterialTheme.shapes.small)
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Text(text = product.productName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "${product.brand} • ${product.variants.size} variants",
            fontSize = 11.sp,
            color = Color(0xFF666666)
        )
    }
}

@Composable
private fun VariantDraftPanel(
    draft: AddVariantResultDto,
    actionText: String,
    onActionTextChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit
) {
    val variant = draft.variant
    val isColorMissing = draft.status?.equals("ColorNotFound", ignoreCase = true) == true
    val showVariantDetails = !isColorMissing && variant != null

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = if (isColorMissing) "Color Required" else "Variant Draft",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.Black
            )
            draft.message?.let {
                Text(text = it, fontSize = 12.sp, color = Color(0xFF666666))
            }
            if (showVariantDetails) {
                VariantInfoRow(label = "Product ID", value = variant?.productId)
                VariantInfoRow(label = "Size", value = variant?.size?.toPlainString())
                VariantInfoRow(label = "Color", value = variant?.colorName)
                VariantInfoRow(label = "Stock", value = variant?.stock?.toString())
                VariantInfoRow(label = "Price", value = variant?.price?.toPlainString())
            }

            OutlinedTextField(
                value = actionText,
                onValueChange = onActionTextChange,
                label = { Text("Action to send") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onSubmit,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Text(text = "Send to AI", color = Color.White)
                }
                TextButton(onClick = onDismiss) {
                    Text(text = "Dismiss")
                }
            }
        }
    }
}

@Composable
private fun VariantInfoRow(label: String, value: String?) {
    if (value.isNullOrBlank()) return
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, fontSize = 11.sp, color = Color(0xFF777777))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

private fun buildVariantActionText(draft: AddVariantResultDto?): String {
    val status = draft?.status?.lowercase()
    if (status == "colornotfound") {
        val productId = draft.variant?.productId
        return if (productId.isNullOrBlank()) {
            "Add color name"
        } else {
            "Add color name for productId=$productId"
        }
    }
    val variant = draft?.variant ?: return "Confirm add variant"
    val parts = mutableListOf<String>()
    variant.productId?.let { parts.add("productId=$it") }
    variant.sizeId?.let { parts.add("sizeId=$it") }
    variant.colorId?.let { parts.add("colorId=$it") }
    variant.stock?.let { parts.add("stock=$it") }
    variant.price?.let { parts.add("price=${it.toPlainString()}") }
    return if (parts.isEmpty()) "Confirm add variant" else "Confirm add variant (${parts.joinToString(", ")})"
}