package com.example.shoestoreapp.features.admin.voucher.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestoreapp.features.admin.voucher.data.remote.CreateVoucherDto
import com.example.shoestoreapp.features.admin.voucher.data.remote.ResponseVoucherAdminDto
import com.example.shoestoreapp.features.admin.voucher.data.repositories.VoucherRepository
import com.example.shoestoreapp.features.admin.voucher.data.repositories.VoucherRepositoryImpl
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VoucherViewModel(
    private val repository: VoucherRepository = VoucherRepositoryImpl()
) : ViewModel() {
    private val _uiState = MutableStateFlow(VoucherUiState())
    val uiState: StateFlow<VoucherUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<VoucherUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    private val _vouchers = MutableStateFlow<List<ResponseVoucherAdminDto>>(emptyList())
    val vouchers: StateFlow<List<ResponseVoucherAdminDto>> = _vouchers.asStateFlow()

    private var currentPage = 1
    private var isLastPage = false
    private var isLoadingList = false

    init {
        loadVouchers(true)
    }

    fun updateVoucherName(value: String) {
        _uiState.update { it.copy(voucherName = value) }
    }

    fun updateDescription(value: String) {
        _uiState.update { it.copy(description = value) }
    }

    fun updateTargetApplication(value: Int) {
        _uiState.update { it.copy(targetApplication = value) }
    }

    fun updateDiscountStyle(value: Int) {
        _uiState.update { it.copy(discountStyle = value) }
    }

    fun updateDiscountValue(value: String) {
        _uiState.update { it.copy(discountValue = value) }
    }

    fun updateMaxReduction(value: String) {
        _uiState.update { it.copy(maxReduction = value) }
    }

    fun updateMinOrder(value: String) {
        _uiState.update { it.copy(minOrder = value) }
    }

    fun updateTotalQuantity(value: String) {
        _uiState.update { it.copy(totalQuantity = value) }
    }

    fun updateMaxUsagePerUser(value: String) {
        _uiState.update { it.copy(maxUsagePerUser = value) }
    }

    fun updateValidFrom(value: String) {
        _uiState.update { it.copy(validFrom = value) }
    }

    fun updateValidTo(value: String) {
        _uiState.update { it.copy(validTo = value) }
    }

    fun onCreateVoucherClick() {
        val state = _uiState.value
        if (state.isLoading) return

        val voucherName = state.voucherName.trim()
        if (voucherName.isBlank()) {
            _uiEvent.trySend(VoucherUiEvent.ShowError("Voucher name is required."))
            return
        }
        if (state.validFrom.isBlank() || state.validTo.isBlank()) {
            _uiEvent.trySend(VoucherUiEvent.ShowError("Please select valid dates."))
            return
        }

        val discount = state.discountValue.trim().toDoubleOrNull()
        if (discount == null || discount <= 0.0) {
            _uiEvent.trySend(VoucherUiEvent.ShowError("Discount value is invalid."))
            return
        }

        val minOrder = state.minOrder.trim().toDoubleOrNull()
        if (minOrder == null || minOrder < 0.0) {
            _uiEvent.trySend(VoucherUiEvent.ShowError("Minimum order value is invalid."))
            return
        }

        val totalQuantity = state.totalQuantity.trim().toIntOrNull()
        if (totalQuantity == null || totalQuantity <= 0) {
            _uiEvent.trySend(VoucherUiEvent.ShowError("Total quantity is invalid."))
            return
        }

        val maxUsagePerUser = state.maxUsagePerUser.trim().toIntOrNull()
        if (maxUsagePerUser == null || maxUsagePerUser <= 0) {
            _uiEvent.trySend(VoucherUiEvent.ShowError("Max usage per user is invalid."))
            return
        }

        val maxPriceDiscount = if (state.discountStyle == 0) {
            val parsed = state.maxReduction.trim().toDoubleOrNull()
            if (parsed == null || parsed <= 0.0) {
                _uiEvent.trySend(VoucherUiEvent.ShowError("Max reduction is invalid."))
                return
            }
            parsed
        } else {
            0.0
        }

        val dto = CreateVoucherDto(
            voucherName = voucherName,
            voucherDescription = state.description.trim(),
            voucherScope = state.targetApplication,
            discountType = state.discountStyle,
            discount = discount,
            maxPriceDiscount = maxPriceDiscount,
            minOrderPrice = minOrder,
            totalQuantity = totalQuantity,
            maxUsagePerUser = maxUsagePerUser,
            validFrom = state.validFrom.trim(),
            validTo = state.validTo.trim()
        )

        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val result = repository.createVoucher(dto)
            _uiState.update { it.copy(isLoading = false) }
            result.onSuccess {
                _uiEvent.send(VoucherUiEvent.ShowSuccess("Voucher created."))
                _uiState.update { VoucherUiState() }
                loadVouchers(true)
            }.onFailure { error ->
                _uiEvent.send(
                    VoucherUiEvent.ShowError(error.message ?: "Unable to create voucher.")
                )
            }
        }
    }

    fun loadVouchers(isRefresh: Boolean = false) {
        if (isRefresh) {
            currentPage = 1
            isLastPage = false
            _vouchers.value = emptyList()
        }
        if (isLoadingList || (!isRefresh && isLastPage)) return

        val pageToLoad = currentPage
        isLoadingList = true
        viewModelScope.launch {
            val result = repository.getVouchers(pageToLoad, DEFAULT_PAGE_SIZE)
            result.onSuccess { response ->
                val updated = if (isRefresh) {
                    response.items
                } else {
                    _vouchers.value + response.items
                }
                _vouchers.value = updated
                isLastPage = !response.hasNext
                if (response.hasNext) {
                    currentPage = pageToLoad + 1
                }
            }.onFailure { error ->
                _uiEvent.send(
                    VoucherUiEvent.ShowError(error.message ?: "Unable to load vouchers.")
                )
            }
            isLoadingList = false
        }
    }

    companion object {
        private const val DEFAULT_PAGE_SIZE = 10
    }
}
