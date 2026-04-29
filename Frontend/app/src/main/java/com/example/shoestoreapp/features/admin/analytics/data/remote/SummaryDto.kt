package com.example.shoestoreapp.features.admin.analytics.data.remote

/**
 * Data Transfer Object for revenue summary statistics
 * Represents the response from /api/statistics/summary endpoint
 */
data class SummaryDto(
    val totalRevenue: Double,                 // Total revenue amount
    val totalOrders: Int,                     // Total number of orders
    val averageRevenue: Double,               // Average ticket value
    val growthInvoicePercent: Double,         // Growth % for orders (e.g., 8.0 for +8%)
    val growthAverageRevenuePercent: Double,  // Growth % for average revenue
    val growthTotalRevenuePercent: Double     // Growth % for total revenue
)
