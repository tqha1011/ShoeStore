package com.example.shoestoreapp.features.admin.analytics.data.remote

/**
 * Data Transfer Object for chart data
 * Represents a single data point in the revenue chart
 */
data class ChartDataDto(
    val dateLabel: String,  // Date label (e.g., "Mon", "Tue", "2024-05-15")
    val revenue: Double     // Revenue for this date
)
