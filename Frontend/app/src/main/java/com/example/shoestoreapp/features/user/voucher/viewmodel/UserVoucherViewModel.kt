package com.example.shoestoreapp.features.user.voucher.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestoreapp.features.user.voucher.data.repositories.UserVoucherRepository
import com.example.shoestoreapp.features.user.voucher.data.repositories.UserVoucherRepositoryImpl
import com.example.shoestoreapp.features.user.voucher.data.models.VoucherUiModel
import com.example.shoestoreapp.features.user.voucher.data.remote.toUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UserVoucherUiState(
    val isLoading: Boolean = false,
    val vouchers: List<VoucherUiModel> = emptyList()
)

class UserVoucherViewModel(
    private val repository: UserVoucherRepository = UserVoucherRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserVoucherUiState())
    val uiState: StateFlow<UserVoucherUiState> = _uiState.asStateFlow()

    init {
        fetchVouchers(pageIndex = 1, pageSize = 50)
    }

    private fun fetchVouchers(pageIndex: Int, pageSize: Int) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val result = repository.getUserVouchers(pageIndex, pageSize)
            result
                .onSuccess { response ->
                    val mapped = response.items.orEmpty().map { it.toUiModel() }
                    _uiState.update { it.copy(isLoading = false, vouchers = mapped) }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoading = false, vouchers = emptyList()) }
                }
        }
    }
}
