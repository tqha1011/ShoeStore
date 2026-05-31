package com.example.shoestoreapp.features.user.voucher.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestoreapp.features.user.voucher.data.remote.toUiModel
import com.example.shoestoreapp.features.user.voucher.data.models.VoucherUiModel
import com.example.shoestoreapp.features.user.voucher.data.repositories.VoucherRepository
import com.example.shoestoreapp.features.user.voucher.data.repositories.VoucherRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CollectVoucherUiState(
    val vouchers: List<VoucherUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val currentPage: Int = 1,
    val hasNextPage: Boolean = true
)

class CollectVoucherViewModel(
    private val repository: VoucherRepository = VoucherRepositoryImpl()
) : ViewModel() {
    private val _uiState = MutableStateFlow(CollectVoucherUiState())
    val uiState: StateFlow<CollectVoucherUiState> = _uiState.asStateFlow()

    init {
        fetchVouchers()
    }

    fun fetchVouchers(isLoadMore: Boolean = false) {
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
            val result = repository.getValidVouchers(pageToLoad, DEFAULT_PAGE_SIZE)
            result.onSuccess { response ->
                // THÊM TRY-CATCH
                try {
                    val newItems = response.items.map { it.toUiModel() }
                    android.util.Log.d("VoucherDebug", "Map thành công ${newItems.size} cái voucher!")

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
                            hasNextPage = response.hasNext,
                            currentPage = if (response.hasNext) pageToLoad + 1 else pageToLoad
                        )
                    }
                } catch (e: Exception) {
                    // NẾU MAPPER BỊ LỖI, NÓ SẼ KHAI BÁO Ở ĐÂY:
                    android.util.Log.e("VoucherDebug", "LỖI TẠI MAPPER RỒI ĐẠI CA ƠI: ", e)
                }
            }.onFailure { throwable ->
                _uiState.update { state ->
                    state.copy(isLoading = false, isLoadingMore = false)
                }
            }
        }
    }

    fun markCollected(
        voucherId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.claimVoucher(voucherId)
            result.onSuccess {
                _uiState.update { state ->
                    state.copy(
                        vouchers = state.vouchers.map { voucher ->
                            if (voucher.id == voucherId) {
                                voucher.copy(isCollected = true)
                            } else {
                                voucher
                            }
                        }
                    )
                }
                onSuccess()
            }.onFailure { throwable ->
                onError(throwable.message.orEmpty())
            }
        }
    }

    companion object {
        private const val DEFAULT_PAGE_SIZE = 10
    }
}
