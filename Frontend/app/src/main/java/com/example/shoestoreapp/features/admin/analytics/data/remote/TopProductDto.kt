package com.example.shoestoreapp.features.admin.analytics.data.remote

/**
 * Data Transfer Object for top-selling products
 * Represents a single product in the top sales list
 */
data class TopProductDto(
    val productPublicId: String,        // Unique product identifier
    val productName: String,            // Product name (e.g., "Premium White Sneaker")
    val imageUrl: String?,              // Product image URL (nullable)
    val totalInvoices: Int,             // Total number of orders
    val totalRevenue: Double,           // Total revenue from this product
    val growthRevenuePercentage: Double // Growth percentage (e.g., 18.5 for +18.5%)
)
