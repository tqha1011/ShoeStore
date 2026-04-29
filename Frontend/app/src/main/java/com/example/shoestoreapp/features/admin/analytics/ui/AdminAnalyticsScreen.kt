package com.example.shoestoreapp.features.admin.analytics.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.shoestoreapp.features.admin.analytics.data.remote.ChartDataDto
import com.example.shoestoreapp.features.admin.analytics.data.remote.SummaryDto
import com.example.shoestoreapp.features.admin.analytics.data.remote.TopProductDto
import com.example.shoestoreapp.features.admin.product.ui.components.AdminBottomNavBar
import com.example.shoestoreapp.features.admin.product.ui.components.AdminBottomNavTab
import com.example.shoestoreapp.features.admin.analytics.viewmodel.AdminAnalyticsState
import com.example.shoestoreapp.features.admin.analytics.viewmodel.AdminAnalyticsViewModel
import java.text.NumberFormat
import java.util.Locale

/**
 * Admin Revenue Analytics Screen
 * Displays comprehensive analytics dashboard with revenue trends, top products, and growth insights
 */
@Composable
fun AdminAnalyticsScreen(
    viewModel: AdminAnalyticsViewModel,
    onTabSelected: (AdminBottomNavTab) -> Unit = {}
) {
    val state: AdminAnalyticsState = viewModel.state
    val summary = state.summary
    val chartData = state.chartData
    val topProducts = state.topProducts

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top Navigation Bar
        AdminAnalyticsTopBar(
            onAiClick = {
               // Handle AI Assistant click
            }
        )

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Loading analytics...", color = Color(0xFF666666), fontSize = 14.sp)
            }
            return
        }

        if (summary == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "No analytics data available.", color = Color(0xFF666666), fontSize = 14.sp)
            }
            return
        }

        val safeIndex = if (chartData.isEmpty()) 0 else state.selectedIndex.coerceIn(0, chartData.lastIndex)

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            if (!state.error.isNullOrBlank()) {
                item {
                    Text(
                        text = state.error,
                        color = Color(0xFFB00020),
                        fontSize = 12.sp
                    )
                }
            }

            // Header Section
            item {
                AdminAnalyticsHeader()
            }

            // Key Metrics Grid
            item {
                KeyMetricsGrid(summary = summary)
            }

            // Performance Trends Chart
            item {
                PerformanceTrendsSection(
                    chartData = chartData,
                    selectedIndex = safeIndex,
                    selectedPeriod = state.chartPeriod,
                    onPeriodSelected = { viewModel.selectChartPeriod(it) },
                    onBarSelected = { viewModel.selectBar(it) }
                )
            }

            // Top Sales Products
            item {
                TopSalesProductsSection(topProducts = topProducts)
            }

            // Growth Opportunity Card
            item {
                summary?.let { data ->
                    GrowthOpportunityCard(
                        growthPercent = data.growthTotalRevenuePercent,
                        onGenerateCampaignClick = {
                            val topProductsText = topProducts.take(3).joinToString(separator = "\n") { product ->
                                "- ${product.productName}: ${formatCount(product.totalInvoices)} orders (Revenue: ${formatCurrency(product.totalRevenue)}, Trend: ${formatPercent(product.growthRevenuePercentage)})"
                            }

                            val aiPrompt = """
                                Act as an expert Chief Marketing Officer for my Shoe E-commerce App. 
                                Analyze our exact performance metrics from last month and provide 3 highly tailored, data-driven marketing strategies for this month.

                                [OVERALL PERFORMANCE]
                                - Total Revenue: ${formatCurrency(data.totalRevenue)} (Trend: ${formatPercent(data.growthTotalRevenuePercent)})
                                - Total Orders: ${formatCount(data.totalOrders)} (Trend: ${formatPercent(data.growthInvoicePercent)})
                                - Average Order Value: ${formatCurrency(data.averageRevenue)} (Trend: ${formatPercent(data.growthAverageRevenuePercent)})

                                [TOP 3 BEST-SELLING PRODUCTS]
                                $topProductsText

                                [YOUR TASK]
                                Look for correlations in the data (e.g., if orders are up but average value is down, or how to leverage the top-selling shoes to boost overall sales). Give me 3 concrete, actionable campaigns to execute right now.
                            """.trimIndent()

                            // Sếp gắn logic đẩy sang màn hình AI vào đây nhé
                            println("SEND TO AI: \n$aiPrompt")
                        }
                    )
                }
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Bottom Navigation Bar
        AdminBottomNavBar(selectedTab = AdminBottomNavTab.ANALYTICS, onTabSelected = onTabSelected)
    }
}

/**
 * Top navigation bar with menu, title, and search
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAnalyticsTopBar(
    onAiClick: () -> Unit = {}
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "SHOE STORE",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color.Black,
                letterSpacing = 1.sp
            )
        },
        actions = {
            IconButton(onClick = onAiClick) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome, // Icon for AI Assistant
                    contentDescription = "AI Assistant",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.White,
            titleContentColor = Color.Black,
            navigationIconContentColor = Color.Black,
            actionIconContentColor = Color.Black
        ),
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(top = 8.dp)
    )
}

/**
 * Header section with "ADMINISTRATION" overline and "Revenue Analytics" title
 */
@Composable
fun AdminAnalyticsHeader() {
    Column {
        Text(
            text = "ADMINISTRATION",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF999999),
            letterSpacing = 0.8.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Revenue Analytics",
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.Black
        )
    }
}

/**
 * 2-column grid displaying total revenue, total orders, and average ticket
 * Uses growth percentages to determine trend colors (green for positive, red for negative)
 */
@Composable
fun KeyMetricsGrid(summary: SummaryDto) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Top Row - Total Revenue (Full Width)
        MetricCard(
            label = "TOTAL REVENUE",
            value = formatCurrency(summary.totalRevenue),
            trendValue = summary.growthTotalRevenuePercent,
            modifier = Modifier.fillMaxWidth()
        )

        // Bottom Row - 2 Columns
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                label = "TOTAL ORDERS",
                value = formatCount(summary.totalOrders),
                trendValue = summary.growthInvoicePercent,
                modifier = Modifier.weight(0.35f)
            )
            MetricCard(
                label = "AVG TICKET",
                value = formatCurrency(summary.averageRevenue),
                trendValue = summary.growthAverageRevenuePercent,
                modifier = Modifier.weight(0.65f)
            )
        }
    }
}

/**
 * Individual metric card component
 * Displays label, value, and trend with automatic color coding
 */
@Composable
fun MetricCard(
    label: String,
    value: String,
    trendValue: Double,
    modifier: Modifier = Modifier
) {
    // Determine trend color: green if positive, red if negative
    val trendColor = when {
        trendValue > 0.0 -> Color(0xFF22C55E)
        trendValue < 0.0 -> Color(0xFFEF4444)
        else -> Color(0xFF999999)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier
            .border(1.dp, Color(0xFFE8E8E8), shape = RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF999999),
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
                Text(
                    text = formatPercent(trendValue),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = trendColor
                )
            }
        }
    }
}

/**
 * Performance Trends section with interactive bar chart
 * Clicking on a bar updates the selectedIndex and displays its revenue
 */
@Composable
fun PerformanceTrendsSection(
    chartData: List<ChartDataDto>,
    selectedIndex: Int,
    selectedPeriod: String,
    onPeriodSelected: (String) -> Unit,
    onBarSelected: (Int) -> Unit
) {
    Column {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Performance Trends",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            ChartPeriodSwitcher(
                selectedPeriod = selectedPeriod,
                onPeriodSelected = onPeriodSelected
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chart Area
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp)
                .border(1.dp, Color(0xFFE8E8E8), shape = RoundedCornerShape(12.dp))
                .padding(16.dp),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Active Revenue Peak Info
                Text(
                    text = "ACTIVE REVENUE PEAK",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF999999),
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = formatCurrency(chartData[selectedIndex].revenue),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
                Text(
                    text = chartData[selectedIndex].dateLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF666666)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Bar Chart
                SparklineChart(
                    data = chartData,
                    selectedIndex = selectedIndex,
                    onPointSelected = onBarSelected,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun ChartPeriodSwitcher(
    selectedPeriod: String,
    onPeriodSelected: (String) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ChartPeriodItem(label = "7D", value = "7days", selectedPeriod = selectedPeriod, onClick = onPeriodSelected)
        ChartPeriodItem(label = "30D", value = "30days", selectedPeriod = selectedPeriod, onClick = onPeriodSelected)
        ChartPeriodItem(label = "12M", value = "12months", selectedPeriod = selectedPeriod, onClick = onPeriodSelected)
    }
}

@Composable
fun ChartPeriodItem(
    label: String,
    value: String,
    selectedPeriod: String,
    onClick: (String) -> Unit
) {
    val isSelected = value == selectedPeriod
    Text(
        text = label,
        fontSize = 12.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        color = if (isSelected) Color.Black else Color(0xFF999999),
        letterSpacing = 0.6.sp,
        modifier = Modifier
            .clickable { onClick(value) }
            .padding(horizontal = 4.dp, vertical = 2.dp)
    )
}

/**
 * Sparkline chart component
 * Renders a line chart with points for each data value
 * Selected point is highlighted, others are light gray
 */
@Composable
fun SparklineChart(
    data: List<ChartDataDto>,
    selectedIndex: Int,
    onPointSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val revenues = data.map { it.revenue }
    val maxRevenue = revenues.maxOrNull() ?: 1.0
    val minRevenue = revenues.minOrNull() ?: 0.0
    val pointSpacing =50.dp
    val scrollState = rememberScrollState()
    val contentWidth = pointSpacing * data.size
    // Auto-scroll to the far right (latest date) when data updates
    LaunchedEffect(data) {
        if (data.isNotEmpty()) {
            scrollState.scrollTo(scrollState.maxValue)
        }
    }
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .horizontalScroll(scrollState)
        ) {
            Box(
                modifier = Modifier
                    .width(contentWidth)
                    .fillMaxHeight()
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (data.isEmpty()) return@Canvas

                    val height = size.height
                    val paddingTop = 16.dp.toPx()
                    val paddingBottom = 16.dp.toPx()
                    val usableHeight = height - paddingTop - paddingBottom
                    val range = (maxRevenue - minRevenue).takeIf { it > 0.0 } ?: 1.0
                    val spacingPx = pointSpacing.toPx()

                    val points = data.mapIndexed { index, item ->
                        val ratio = ((item.revenue - minRevenue) / range).toFloat()
                        val x = (index * spacingPx) + (spacingPx / 2f)
                        val y = (paddingTop + usableHeight) - (usableHeight * ratio)
                        Offset(x, y)
                    }

                    val path = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        points.drop(1).forEach { lineTo(it.x, it.y) }
                    }

                    drawPath(
                        path = path,
                        color = Color.Black,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    points.forEachIndexed { index, point ->
                        val isSelected = index == selectedIndex
                        drawCircle(
                            color = if (isSelected) Color.Black else Color(0xFFD1D1D1),
                            radius = (if (isSelected) 5.dp else 4.dp).toPx(),
                            center = point
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxSize()) {
                    data.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { onPointSelected(index) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            data.forEach { chartItem ->
                Box(
                    modifier = Modifier
                        .width(pointSpacing),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = chartItem.dateLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF666666),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * Top Sales Products section
 * Displays list of best-selling products with revenue and order counts
 */
@Composable
fun TopSalesProductsSection(topProducts: List<TopProductDto>) {
    Column {
        Text(
            text = "Top Sales Products",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            topProducts.forEach { product ->
                TopProductCard(product = product)
            }
        }
    }
}

/**
 * Individual product card in top sales list
 * Shows product image, name, order count, revenue, and growth percentage
 */
@Composable
fun TopProductCard(product: TopProductDto) {
    // Determine growth color
    val growthColor = when {
        product.growthRevenuePercentage > 0.0 -> Color(0xFF22C55E)
        product.growthRevenuePercentage < 0.0 -> Color(0xFFEF4444)
        else -> Color(0xFF999999)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, shape = RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFE8E8E8), shape = RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Product Image
        AsyncImage(
            model = product.imageUrl,
            contentDescription = product.productName,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFEEEEEE)),
            contentScale = ContentScale.Crop
        )

        // Product Info (Middle)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = product.productName,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${formatCount(product.totalInvoices)} ORDERS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF999999),
                letterSpacing = 0.3.sp
            )
        }

        // Revenue Info (Right)
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatCurrency(product.totalRevenue),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatPercent(product.growthRevenuePercentage),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = growthColor
            )
        }
    }
}

/**
 * Growth Opportunity Card
 * Displays insights and actionable recommendations for business growth
 */
@Composable
fun GrowthOpportunityCard(
    growthPercent: Double,
    onGenerateCampaignClick: () -> Unit
) {
    val isIncrease = growthPercent >= 0.0
    val trendWord = if (isIncrease) "increased" else "decreased"
    val color = if (isIncrease) Color(0xFF22C55E) else Color(0xFFEF4444)
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE8E8E8), shape = RoundedCornerShape(12.dp))
            .padding(16.dp),
    ) {
        Column {
            // Header with icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = "Growth",
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "GROWTH OPPORTUNITY",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Description
            Text(
                text = "Your total revenue has $trendWord by ${formatPercent(growthPercent)} compared to last month. " +
                        if (isIncrease) "Maximize this momentum now!" else "Action required to recover your sales.",
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF555555),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action Button
            Text(
                text = "GENERATE CAMPAIGN",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textDecoration = TextDecoration.Underline,
                letterSpacing = 0.5.sp,
                modifier = Modifier.clickable { onGenerateCampaignClick() }
            )
        }
    }
}


/**
 * Format currency value for display
 */
private fun formatCurrency(value: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    return format.format(value)
}

/**
 * Format count value for display
 */
private fun formatCount(value: Int): String {
    return NumberFormat.getIntegerInstance(Locale.US).format(value)
}

/**
 * Format percentage value for display
 */
private fun formatPercent(value: Double): String {
    val sign = when {
        value > 0.0 -> "+"
        value < 0.0 -> "-"
        else -> ""
    }
    val absValue = kotlin.math.abs(value)
    val formatted = if (absValue % 1.0 == 0.0) {
        String.format(Locale.US, "%.0f", absValue)
    } else {
        String.format(Locale.US, "%.1f", absValue)
    }
    return "$sign$formatted%"
}
