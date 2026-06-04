package com.example.shoestoreapp.features.user.voucher.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestoreapp.features.user.voucher.data.models.VoucherUiModel
import com.example.shoestoreapp.features.user.voucher.data.remote.toUiModel
import com.example.shoestoreapp.features.user.voucher.data.repositories.VoucherRepository
import com.example.shoestoreapp.features.user.voucher.data.repositories.VoucherRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyVoucherUiState(
    val vouchers: List<VoucherUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val currentPage: Int = 1,
    val hasNextPage: Boolean = true
)

class MyVoucherViewModel(
    private val repository: VoucherRepository = VoucherRepositoryImpl()
) : ViewModel() {
    private val _uiState = MutableStateFlow(MyVoucherUiState())
    val uiState: StateFlow<MyVoucherUiState> = _uiState.asStateFlow()

    fun fetchMyVouchers(isLoadMore: Boolean = false) {
        val currentState = _uiState.value
        if (currentState.isLoading || currentState.isLoadingMore) return
        if (isLoadMore && !currentState.hasNextPage) return

        val pageToLoad = if (isLoadMore) currentState.currentPage else 1
        _uiState.update {
            it.copy(
                isLoading = !isLoadMore,
                isLoadingMore = isLoadMore
            )
        }

        viewModelScope.launch {
            val result = repository.getMyVouchers(pageToLoad, DEFAULT_PAGE_SIZE)
            result.onSuccess { response ->
                try {
                    val newItems = response.items.map { it.toUiModel() }
                    handleFetchSuccess(newItems, isLoadMore, response.hasNext, pageToLoad)
                } catch (e: Exception) {
                    android.util.Log.e("MyVoucherVM", "Lỗi map dữ liệu", e)
                    handleFetchError()
                }
            }.onFailure { throwable ->
                android.util.Log.e("MyVoucherVM", "Không gọi được API ", throwable)
                handleFetchError()
            }
        }
    }

    // ==========================================
    // CÁC HÀM PHỤ TRỢ (Helper Functions)
    // ==========================================

    private fun handleFetchSuccess(
        newItems: List<VoucherUiModel>,
        isLoadMore: Boolean,
        hasNext: Boolean,
        pageToLoad: Int
    ) {
        _uiState.update { state ->
            val updatedList = if (isLoadMore) {
                state.vouchers + newItems
            } else {
                newItems
            }
            state.copy(
                vouchers = updatedList,
                isLoading = false,
                isLoadingMore = false,
                hasNextPage = hasNext,
                currentPage = if (hasNext) pageToLoad + 1 else pageToLoad
            )
        }
    }

    private fun handleFetchError() {
        _uiState.update { state ->
            state.copy(isLoading = false, isLoadingMore = false)
        }
    }

    companion object {
        private const val DEFAULT_PAGE_SIZE = 10
    }
}