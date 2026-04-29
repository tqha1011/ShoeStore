package com.example.shoestoreapp.features.admin.analytics.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestoreapp.features.admin.analytics.data.AnalyticsRepository
import com.example.shoestoreapp.features.admin.analytics.data.remote.ChartDataDto
import com.example.shoestoreapp.features.admin.analytics.data.remote.SummaryDto
import com.example.shoestoreapp.features.admin.analytics.data.remote.TopProductDto
import kotlinx.coroutines.launch

class AdminAnalyticsViewModel(
    private val repository: AnalyticsRepository
) : ViewModel() {

    var state = AdminAnalyticsState()
        private set

    init {
        loadAnalytics()
    }

    // Loads summary, chart data, and top products into the screen state.
    fun loadAnalytics() {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)

            val summaryResult = repository.getSummary()
            val chartResult = repository.getChartData()
            val topProductsResult = repository.getTopProducts()

            val errorMessage = listOfNotNull(
                summaryResult.exceptionOrNull()?.message,
                chartResult.exceptionOrNull()?.message,
                topProductsResult.exceptionOrNull()?.message
            ).firstOrNull()

            state = state.copy(
                isLoading = false,
                summary = summaryResult.getOrNull(),
                chartData = chartResult.getOrNull().orEmpty(),
                topProducts = topProductsResult.getOrNull().orEmpty(),
                error = errorMessage
            )
        }
    }
}

data class AdminAnalyticsState(
    val isLoading: Boolean = false,
    val summary: SummaryDto? = null,
    val chartData: List<ChartDataDto> = emptyList(),
    val topProducts: List<TopProductDto> = emptyList(),
    val error: String? = null
)

