package com.example.shoestoreapp.features.admin.analytics.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface AnalyticsApi {
    @GET("/api/statistics/summary")
    suspend fun getSummary(): Response<SummaryDto>

    @GET("/api/statistics/chart")
    suspend fun getChartData(
        @Query("type") type: String
    ): Response<ChartResponseDto>

    @GET("/api/statistics/top-products")
    suspend fun getTopProducts(): Response<List<TopProductDto>>
}
