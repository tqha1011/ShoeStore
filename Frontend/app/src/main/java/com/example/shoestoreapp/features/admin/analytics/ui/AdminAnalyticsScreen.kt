package com.example.shoestoreapp.features.admin.analytics.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

/**
 * Admin Revenue Analytics Screen
 * Displays comprehensive analytics dashboard with revenue trends, top products, and growth insights
 */
@Composable
fun AdminAnalyticsScreen(
    onTabSelected: (AdminBottomNavTab) -> Unit = {},
    summary: SummaryDto = getMockSummaryData(),
    chartData: List<ChartDataDto> = getMockChartData(),
    topProducts: List<TopProductDto> = getMockTopProductsData()
) {
    var selectedChartIndex by remember { mutableIntStateOf(2) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top Navigation Bar
        AdminAnalyticsTopBar()

        // Main Content
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
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
                    selectedIndex = selectedChartIndex,
                    onBarSelected = { selectedChartIndex = it }
                )
            }

            // Top Sales Products
            item {
                TopSalesProductsSection(topProducts = topProducts)
            }

            // Growth Opportunity Card
            item {
                GrowthOpportunityCard()
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
@Composable
fun AdminAnalyticsTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(1.dp, Color(0xFFE8E8E8))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.Black)
        Text("SHOE STORE", color = Color.Black, fontWeight = FontWeight.Black, letterSpacing = 1.6.sp)
        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF666666))
    }
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
            value = "$${summary.totalRevenue}",
            trend = summary.growthTotalRevenuePercent,
            modifier = Modifier.fillMaxWidth()
        )

        // Bottom Row - 2 Columns
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                label = "TOTAL ORDERS",
                value = summary.totalOrders,
                trend = summary.growthInvoicePercent,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                label = "AVG TICKET",
                value = "$${summary.averageRevenue}",
                trend = summary.growthAverageRevenuePercent,
                modifier = Modifier.weight(1f)
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
    trend: String,
    modifier: Modifier = Modifier
) {
    // Determine trend color: green if positive, red if negative
    val trendColor = if (trend.contains("+") || trend.contains("-") && !trend.contains("-")) {
        Color(0xFF22C55E) // Green for positive
    } else if (trend.contains("-")) {
        Color(0xFFEF4444) // Red for negative
    } else {
        Color(0xFF22C55E) // Default to green
    }

    Card(
        modifier = modifier
            .background(Color.White, shape = RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFE8E8E8), shape = RoundedCornerShape(12.dp))
            .padding(16.dp),
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
                    text = trend,
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
    onBarSelected: (Int) -> Unit
) {
    Column {
        // Header
        Text(
            text = "Performance Trends",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Chart Area
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, shape = RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFFE8E8E8), shape = RoundedCornerShape(12.dp))
                .padding(16.dp),
        ) {
            Column {
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
                    text = "$${chartData[selectedIndex].revenue}",
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

                Spacer(modifier = Modifier.height(24.dp))

                // Bar Chart
                InteractiveBarChart(
                    data = chartData,
                    selectedIndex = selectedIndex,
                    onBarSelected = onBarSelected
                )
            }
        }
    }
}

/**
 * Interactive bar chart component
 * Renders 7 bars with heights based on revenue values
 * Selected bar is black, others are light gray
 */
@Composable
fun InteractiveBarChart(
    data: List<ChartDataDto>,
    selectedIndex: Int,
    onBarSelected: (Int) -> Unit
) {
    // Parse revenue strings to floats for height calculation
    val revenueValues = data.mapNotNull { it.revenue.toFloatOrNull() }
    val maxRevenue = revenueValues.maxOrNull() ?: 1f
    val minHeight = 30.dp
    val maxHeight = 180.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEachIndexed { index, chartItem ->
            val revenue = chartItem.revenue.toFloatOrNull() ?: 0f
            val heightRatio = if (maxRevenue > 0) revenue / maxRevenue else 0f
            val barHeight = minHeight + (maxHeight - minHeight) * heightRatio
            val barColor = if (index == selectedIndex) Color.Black else Color(0xFFEEEEEE)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(barHeight)
                        .background(barColor, shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .clickable { onBarSelected(index) }
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = chartItem.dateLabel,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF666666)
                )
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
            text = "Top Sales Channels",
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
    val growthColor = if (product.growthRevenuePercentage.contains("+")) {
        Color(0xFF22C55E) // Green for positive
    } else if (product.growthRevenuePercentage.contains("-")) {
        Color(0xFFEF4444) // Red for negative
    } else {
        Color(0xFF22C55E)
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
            contentScale = ContentScale.Crop,
            fallback = androidx.compose.material.icons.filled.ShoppingCart // Fallback icon
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
                text = "${product.totalInvoices} ORDERS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF999999),
                letterSpacing = 0.3.sp
            )
        }

        // Revenue Info (Right)
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$${product.totalRevenue}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = product.growthRevenuePercentage,
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
fun GrowthOpportunityCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, shape = RoundedCornerShape(12.dp))
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
                    imageVector = Icons.Default.TrendingUp,
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
                text = "Your mobile app conversion rate has increased by 4.2% this month. Consider pushing an app-exclusive drop to maximize momentum.",
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
                modifier = Modifier.clickable { /* Handle campaign generation */ }
            )
        }
    }
}

/**
 * Mock data for testing - Total revenue summary
 */
private fun getMockSummaryData(): SummaryDto = SummaryDto(
    totalRevenue = "284910.42",
    totalOrders = "1842",
    averageRevenue = "154.67",
    growthInvoicePercent = "+8%",
    growthAverageRevenuePercent = "-2%",
    growthTotalRevenuePercent = "+12.4%"
)

/**
 * Mock data for testing - Chart data for last 7 days
 */
private fun getMockChartData(): List<ChartDataDto> = listOf(
    ChartDataDto("Mon", "8500.00"),
    ChartDataDto("Tue", "12300.50"),
    ChartDataDto("Wed", "14200.00"),
    ChartDataDto("Thu", "9800.75"),
    ChartDataDto("Fri", "11500.25"),
    ChartDataDto("Sat", "13200.00"),
    ChartDataDto("Sun", "10900.50")
)

/**
 * Mock data for testing - Top 3 selling products
 */
private fun getMockTopProductsData(): List<TopProductDto> = listOf(
    TopProductDto(
        productPublicId = "prod_001",
        productName = "Direct Storefront",
        imageUrl = "https://via.placeholder.com/48?text=Store",
        totalInvoices = "642",
        totalRevenue = "102400.00",
        growthRevenuePercentage = "+18.5%"
    ),
    TopProductDto(
        productPublicId = "prod_002",
        productName = "Mobile App",
        imageUrl = "https://via.placeholder.com/48?text=App",
        totalInvoices = "512",
        totalRevenue = "84120.50",
        growthRevenuePercentage = "+24.1%"
    ),
    TopProductDto(
        productPublicId = "prod_003",
        productName = "Social Referral",
        imageUrl = "https://via.placeholder.com/48?text=Social",
        totalInvoices = "289",
        totalRevenue = "42900.00",
        growthRevenuePercentage = "-2.4%"
    )
)

// Import for content scale
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.foundation.layout.ContentScale

