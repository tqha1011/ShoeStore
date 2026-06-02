package com.example.shoestoreapp.features.admin.analytics.data.remote

/**
 * Wrapper for chart data list returned by /api/statistics/chart
 */
data class ChartResponseDto(
    val chartData: List<ChartDataDto> = emptyList()
)

