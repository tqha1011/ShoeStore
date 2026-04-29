package com.example.shoestoreapp.features.admin.analytics.data

import com.example.shoestoreapp.features.admin.analytics.data.remote.AnalyticsApi
import com.example.shoestoreapp.features.admin.analytics.data.remote.ChartDataDto
import com.example.shoestoreapp.features.admin.analytics.data.remote.SummaryDto
import com.example.shoestoreapp.features.admin.analytics.data.remote.TopProductDto

class AnalyticsRepository(private val api: AnalyticsApi) {
    // Fetches the revenue summary.
    suspend fun getSummary(): Result<SummaryDto> = runCatching {
        val response = api.getSummary()
        if (response.isSuccessful) {
            response.body() ?: throw Exception("Summary response is empty")
        } else {
            throw Exception("Summary API error: ${response.code()} - ${response.message()}")
        }
    }

    // Fetches the chart data for revenue trends.
    suspend fun getChartData(type: String = "7days"): Result<List<ChartDataDto>> = runCatching {
        val response = api.getChartData(type)
        if (response.isSuccessful) {
            response.body()?.chartData.orEmpty()
        } else {
            throw Exception("Chart API error: ${response.code()} - ${response.message()}")
        }
    }

    // Fetches the top selling products list.
    suspend fun getTopProducts(): Result<List<TopProductDto>> = runCatching {
        val response = api.getTopProducts()
        if (response.isSuccessful) {
            response.body().orEmpty()
        } else {
            throw Exception("Top products API error: ${response.code()} - ${response.message()}")
        }
    }
}
