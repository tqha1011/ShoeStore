package com.example.shoestoreapp.features.admin.analytics.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestoreapp.features.admin.analytics.data.AnalyticsRepository
import com.example.shoestoreapp.features.admin.analytics.data.remote.ChartDataDto
import com.example.shoestoreapp.features.admin.analytics.data.remote.SummaryDto
import com.example.shoestoreapp.features.admin.analytics.data.remote.TopProductDto
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AdminAnalyticsViewModel(
    private val repository: AnalyticsRepository
) : ViewModel() {

    var state by mutableStateOf(AdminAnalyticsState())
        private set
    private var chartJob: Job? = null // Tracks the current chart loading job for cancellation.

    init {
        loadAnalytics()
    }

    // Loads summary, chart data, and top products into the screen state.
    fun loadAnalytics() {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)

            val summaryResult = repository.getSummary()
            val chartResult = repository.getChartData(state.chartPeriod)
            val topProductsResult = repository.getTopProducts()

            val errorMessage = listOfNotNull(
                summaryResult.exceptionOrNull()?.message,
                chartResult.exceptionOrNull()?.message,
                topProductsResult.exceptionOrNull()?.message
            ).firstOrNull()

            val summaryValue = summaryResult.getOrNull() ?: state.summary
            val chartValue = chartResult.getOrNull().orEmpty().ifEmpty { state.chartData }
            val topProductsValue = topProductsResult.getOrNull().orEmpty().ifEmpty { state.topProducts }
            // Default to the latest date (last index)
            val defaultSelectedIndex = if (chartValue.isNotEmpty()) chartValue.lastIndex else 0
            state = state.copy(
                isLoading = false,
                summary = summaryValue,
                chartData = chartValue,
                topProducts = topProductsValue,
                error = errorMessage,
                selectedIndex = defaultSelectedIndex
            )
        }
    }

    // Updates the selected chart period and refreshes chart data.
    fun selectChartPeriod(period: String) {
        // Ignore if the same period is already selected
        if (state.chartPeriod == period) return
        state = state.copy(chartPeriod = period)   // Update UI immediately for instant feedback
        chartJob?.cancel() // Cancel previous job to prevent API spam (Debounce)
        chartJob = viewModelScope.launch {
            delay(500) // Wait 500ms before loading new data
            loadChartDataOnly(period)
        }
    }
    private suspend fun loadChartDataOnly(period: String) {
        val chartResult = repository.getChartData(period)

        if (chartResult.isSuccess) {
            val newChartData = chartResult.getOrNull().orEmpty()
            val defaultSelectedIndex = if (newChartData.isNotEmpty()) newChartData.lastIndex else 0

            state = state.copy(
                chartData = newChartData,
                selectedIndex = defaultSelectedIndex,
                // Keep the existing error if any, or clear it if successful
                error = null
            )
        } else {
            // Handle error silently or show toast (optional)
            state = state.copy(error = chartResult.exceptionOrNull()?.message)
        }
    }
    // Updates the selected chart bar index.
    fun selectBar(index: Int) {
        state = state.copy(selectedIndex = index)
    }
}

data class AdminAnalyticsState(
    val isLoading: Boolean = false,
    val summary: SummaryDto? = null,
    val chartData: List<ChartDataDto> = emptyList(),
    val topProducts: List<TopProductDto> = emptyList(),
    val error: String? = null,
    val selectedIndex: Int = 2,
    val chartPeriod: String = "7days"
)
