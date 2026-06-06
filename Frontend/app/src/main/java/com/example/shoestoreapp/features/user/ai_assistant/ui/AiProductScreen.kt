package com.example.shoestoreapp.features.user.ai_assistant.ui

import android.os.Build
import android.widget.Toast
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.shoestoreapp.features.admin.product.ui.components.AdminFormField
import com.example.shoestoreapp.features.admin.product.ui.components.AdminShoeColorDropdown
import com.example.shoestoreapp.features.admin.product.ui.components.AdminShoeSizeDropdown
import com.example.shoestoreapp.features.admin.addproduct.data.remote.master_data.ColorDto
import com.example.shoestoreapp.features.admin.addproduct.data.remote.master_data.SizeDto
import com.example.shoestoreapp.features.agent_intelligent.data.remote.AddVariantResultDto
import com.example.shoestoreapp.features.agent_intelligent.data.remote.ProductSummaryForLlm
import com.example.shoestoreapp.features.agent_intelligent.data.remote.SearchProductResultDto
import com.example.shoestoreapp.features.agent_intelligent.data.remote.VariantResultDto
import com.example.shoestoreapp.features.agent_intelligent.ui.BaseAIChatScreen
import com.example.shoestoreapp.features.user.ai_assistant.viewmodel.AiProductViewmodel
import androidx.compose.ui.platform.LocalContext

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AiProductScreen(
    viewModel: AiProductViewmodel,
    initialPrompt: String? = null,
    onBackClick: () -> Unit = {},
    showAdminPanels: Boolean = false,
    title: String = "Product Assistant",
    aiRoleName: String = "AI ASSISTANT",
    userRoleName: String = "USER"
) {
    val searchResult by viewModel.searchResultState.collectAsState()
    val selectedProduct by viewModel.selectedProductState.collectAsState()
    val variantDraft by viewModel.variantDraftState.collectAsState()
    val sizesList by viewModel.sizesList.collectAsState()
    val colorsList by viewModel.colorsList.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(successMessage) {
        val message = successMessage ?: return@LaunchedEffect
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        viewModel.clearSuccessMessage()
    }

    val dismissedPromptId = remember { mutableStateOf<String?>(null) }
    val dismissedSearchMessageId = remember { mutableStateOf<String?>(null) }
    val lastAiMessage = viewModel.state.messages.lastOrNull { !it.isUser }
    val parsedSearchResult = remember(lastAiMessage?.id, lastAiMessage?.displayText) {
        lastAiMessage?.displayText?.let(::parseSearchResultFromMessage)
    }
    val visibleSearchResult = searchResult ?: parsedSearchResult
        ?.takeIf { dismissedSearchMessageId.value != lastAiMessage?.id }
    val shouldShowManualDialog = showAdminPanels &&
        variantDraft == null &&
        selectedProduct != null &&
        dismissedPromptId.value != selectedProduct?.publicId

    val manualDraft = if (shouldShowManualDialog) {
        val productId = selectedProduct?.publicId.orEmpty()
        if (productId.isBlank()) null
        else AddVariantResultDto(
            status = "Draft",
            message = lastAiMessage
                ?.text
                ?.takeIf(::shouldPromptVariantForm)
                ?: "Nhập size, màu, số lượng và giá bán cho biến thể mới.",
            variant = VariantResultDto(productId = productId)
        )
    } else {
        null
    }

    val effectiveDraft = variantDraft ?: manualDraft

    val sizeInputState = remember(effectiveDraft?.variant?.size) {
        mutableStateOf(effectiveDraft?.variant?.size?.toPlainString().orEmpty())
    }
    val sizeIdInputState = remember(effectiveDraft?.variant?.sizeId) {
        mutableStateOf(effectiveDraft?.variant?.sizeId?.toString().orEmpty())
    }
    val colorInputState = remember(effectiveDraft?.variant?.colorName) {
        mutableStateOf(effectiveDraft?.variant?.colorName.orEmpty())
    }
    val colorIdInputState = remember(effectiveDraft?.variant?.colorId) {
        mutableStateOf(effectiveDraft?.variant?.colorId?.toString().orEmpty())
    }
    val stockInputState = remember(effectiveDraft?.variant?.stock) {
        mutableStateOf(effectiveDraft?.variant?.stock?.toString().orEmpty())
    }
    val priceInputState = remember(effectiveDraft?.variant?.price) {
        mutableStateOf(effectiveDraft?.variant?.price?.toPlainString().orEmpty())
    }

    BaseAIChatScreen(
        viewModel = viewModel,
        title = title,
        aiRoleName = aiRoleName,
        userRoleName = userRoleName,
        initialPrompt = initialPrompt,
        onBackClick = onBackClick,
        footerContent = if (showAdminPanels) {
            {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (selectedProduct == null) visibleSearchResult?.let { result ->
                        SearchResultPanel(
                            result = result,
                            onClear = { viewModel.clearSearchResult() },
                            onProductSelected = { product ->
                                if (product.publicId.isNullOrBlank()) {
                                    dismissedSearchMessageId.value = lastAiMessage?.id
                                }
                                viewModel.selectSearchProduct(product)
                            }
                        )
                    }
                }
            }
        } else {
            null
        }
    )
    if (showAdminPanels && effectiveDraft != null) {
        val draft = effectiveDraft ?: return
        val productId = draft.variant?.productId.orEmpty()
        val displayProduct = searchResult
            ?.products
            ?.firstOrNull { it.publicId == productId }
            ?: selectedProduct
        val productName = displayProduct?.productName
        VariantConfirmDialog(
            draft = draft,
            productName = productName,
            sizeInput = sizeInputState.value,
            onSizeChange = { sizeInputState.value = it },
            onSizeIdChange = { sizeIdInputState.value = it },
            colorInput = colorInputState.value,
            onColorChange = { colorInputState.value = it },
            onColorIdChange = { colorIdInputState.value = it },
            sizesList = sizesList,
            colorsList = colorsList,
            stockInput = stockInputState.value,
            onStockChange = { stockInputState.value = it },
            priceInput = priceInputState.value,
            onPriceChange = { priceInputState.value = it },
            onConfirm = {
                viewModel.addVariantViaApi(
                    productId = productId,
                    sizeId = sizeIdInputState.value.toIntOrNull(),
                    colorId = colorIdInputState.value.toIntOrNull(),
                    sizeValue = sizeInputState.value,
                    colorName = colorInputState.value,
                    stockText = stockInputState.value,
                    priceText = priceInputState.value
                )
            },
            onDismiss = {
                if (variantDraft != null) viewModel.clearVariantDraft()
                else viewModel.clearSelectedProduct()
                dismissedPromptId.value = productId
            }
        )
    }
}

private fun shouldPromptVariantForm(text: String): Boolean {
    val normalized = text.lowercase()
    val keywords = listOf("kích thước", "size", "màu", "color", "stock", "giá", "price")
    return keywords.any { normalized.contains(it) }
}

private fun parseSearchResultFromMessage(text: String): SearchProductResultDto? {
    if (!text.contains("Tìm thấy nhiều", ignoreCase = true) &&
        !text.contains("chọn một", ignoreCase = true)
    ) {
        return null
    }

    val products = parseProductOptions(text)
    if (products.isEmpty()) return null

    return SearchProductResultDto(
        status = "MultipleFound",
        message = "Chọn một sản phẩm để tiếp tục",
        products = products
    )
}

private fun parseProductOptions(text: String): List<ProductSummaryForLlm> {
    val optionRegex = Regex("^\\s*\\d+[.)]\\s+(.+)$")
    val products = mutableListOf<ProductSummaryForLlm>()
    var currentName: String? = null

    fun flushCurrent() {
        val name = currentName?.trim().orEmpty()
        if (name.isNotBlank()) {
            products += ProductSummaryForLlm(productName = name)
        }
        currentName = null
    }

    text.lineSequence().forEach { rawLine ->
        val line = rawLine.trim()
        val match = optionRegex.find(line)
        when {
            match != null -> {
                flushCurrent()
                currentName = match.groupValues[1].trim()
            }
            currentName != null &&
                line.isNotBlank() &&
                !line.endsWith("?") &&
                !line.startsWith("Bạn ", ignoreCase = true) &&
                !line.startsWith("Vui lòng", ignoreCase = true) -> {
                currentName = "$currentName $line"
            }
        }
    }
    flushCurrent()

    return products
}

@Composable
private fun SearchResultPanel(
    result: SearchProductResultDto,
    onClear: () -> Unit,
    onProductSelected: (ProductSummaryForLlm) -> Unit
) {
    val products = result.products.orEmpty().ifEmpty {
        parseProductOptions(result.message.orEmpty())
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (result.status.equals("MultipleFound", ignoreCase = true)) {
                        "Select Product"
                    } else {
                        "Search Results"
                    },
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
                        onClick = { onProductSelected(product) }
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
private fun ProductSearchRow(
    product: ProductSummaryForLlm,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Text(text = product.productName.orEmpty(), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "${product.productBrand.orEmpty()} • ${product.catagoryName.orEmpty()}",
            fontSize = 11.sp,
            color = Color(0xFF666666)
        )
    }
}

@Composable
private fun VariantConfirmDialog(
    draft: AddVariantResultDto,
    productName: String?,
    sizeInput: String,
    onSizeChange: (String) -> Unit,
    onSizeIdChange: (String) -> Unit,
    colorInput: String,
    onColorChange: (String) -> Unit,
    onColorIdChange: (String) -> Unit,
    sizesList: List<SizeDto>,
    colorsList: List<ColorDto>,
    stockInput: String,
    onStockChange: (String) -> Unit,
    priceInput: String,
    onPriceChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 560.dp),
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 30.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Text(
                    text = "Add Variant",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Black
                )
                Text(
                    text = "This will add a variant to the selected product.",
                    fontSize = 18.sp,
                    lineHeight = 26.sp,
                    color = Color(0xFF333333)
                )

                draft.message?.let { message ->
                    Text(
                        text = message,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F1F3), RoundedCornerShape(18.dp))
                            .padding(20.dp),
                        fontSize = 18.sp,
                        lineHeight = 30.sp,
                        fontStyle = FontStyle.Italic,
                        color = Color(0xFF454545)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    productName?.let { name ->
                        DetailInfoRow(label = "PRODUCT NAME:", value = name)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        AdminShoeSizeDropdown(
                            selectedSize = sizeInput,
                            sizesList = sizesList,
                            onSizeSelected = { id, value ->
                                onSizeIdChange(id)
                                onSizeChange(value)
                            }
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        AdminShoeColorDropdown(
                            selectedColor = colorInput,
                            colorsList = colorsList,
                            onColorSelected = { id, value ->
                                onColorIdChange(id)
                                onColorChange(value)
                            }
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        AdminFormField(
                            label = "STOCK",
                            value = stockInput,
                            onValueChange = onStockChange,
                            placeholder = "0",
                            keyboardType = KeyboardType.Number
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        AdminFormField(
                            label = "PRICE ($)",
                            value = priceInput,
                            onValueChange = onPriceChange,
                            placeholder = "0.00",
                            keyboardType = KeyboardType.Decimal
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "Cancel",
                            color = Color.Black,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onConfirm,
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                        modifier = Modifier.height(52.dp)
                    ) {
                        Text(
                            text = "Add",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(0.42f),
            fontSize = 13.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF8A8585)
        )
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            fontSize = 18.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )
    }
}
