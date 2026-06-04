package com.example.shoestoreapp.features.admin.voucher.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestoreapp.features.admin.voucher.data.remote.CreateVoucherDto
import com.example.shoestoreapp.features.admin.voucher.data.remote.ResponseVoucherAdminDto
import com.example.shoestoreapp.features.admin.voucher.data.remote.UpdateVoucherDto
import com.example.shoestoreapp.features.admin.voucher.data.repositories.VoucherRepository
import com.example.shoestoreapp.features.admin.voucher.data.repositories.VoucherRepositoryImpl
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
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

    private val _isEditSheetVisible = MutableStateFlow(false)
    val isEditSheetVisible: StateFlow<Boolean> = _isEditSheetVisible.asStateFlow()

    private val _editingVoucherId = MutableStateFlow<String?>(null)
    val editingVoucherId: StateFlow<String?> = _editingVoucherId.asStateFlow()

    private val _voucherToDelete = MutableStateFlow<String?>(null)
    val voucherToDelete: StateFlow<String?> = _voucherToDelete.asStateFlow()

    private val _showDeleteExpiredDialog = MutableStateFlow(false)
    val showDeleteExpiredDialog: StateFlow<Boolean> = _showDeleteExpiredDialog.asStateFlow()

    private var currentPage = 1
    private var isLastPage = false
    private var isLoadingList = false

    init {
        loadVouchers(true)
    }

    fun hideBanner() {
        _uiState.update { it.copy(showBanner = false) }
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

    fun onReleaseTypeChanged(value: Int) {
        _uiState.update { it.copy(releaseType = value) }
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
            showErrorBanner("Voucher name is required.")
            return
        }

        if (!validateVoucherFields(state)) return

        val discount = state.discountValue.trim().toDouble()
        val minOrder = state.minOrder.trim().toDouble()
        val totalQuantity = state.totalQuantity.trim().toInt()
        val maxUsagePerUser = state.maxUsagePerUser.trim().toInt()
        val maxPriceDiscount = if (state.discountStyle == 2) state.maxReduction.trim().toDouble() else 0.0

        val formattedValidFrom = parseDateToIso(state.validFrom, endOfDay = false)
        val formattedValidTo = parseDateToIso(state.validTo, endOfDay = true)

        if (formattedValidFrom == null || formattedValidTo == null) {
            showErrorBanner("Invalid date format. Please use MM/dd/yyyy or yyyy-MM-dd.")
            return
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
            releaseType = state.releaseType,
            validFrom = formattedValidFrom,
            validTo = formattedValidTo
        )

        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val result = repository.createVoucher(dto)
            _uiState.update { it.copy(isLoading = false) }
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        bannerMessage = "Voucher created.",
                        isBannerSuccess = true,
                        showBanner = true,
                        voucherName = "", description = "", discountValue = "",
                        maxReduction = "", minOrder = "", totalQuantity = "",
                        maxUsagePerUser = "", validFrom = "", validTo = ""
                    )
                }
                loadVouchers(true)
            }.onFailure { error ->
                showErrorBanner(error.message ?: "Action failed.")
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
                showErrorBanner(error.message ?: "Unable to load vouchers.")
            }
            isLoadingList = false
        }
    }

    fun onEditVoucherClick(voucher: ResponseVoucherAdminDto) {
        _editingVoucherId.value = voucher.voucherGuid
        _uiState.update {
            it.copy(
                voucherName = voucher.voucherName,
                description = voucher.voucherDescription.orEmpty(),
                targetApplication = mapVoucherScopeToUi(voucher.voucherScope),
                discountStyle = mapDiscountTypeToUi(voucher.discountType),
                releaseType = 2,
                discountValue = voucher.discount.toString(),
                maxReduction = if (mapDiscountTypeToUi(voucher.discountType) == 2) {
                    voucher.maxPriceDiscount.toString()
                } else {
                    ""
                },
                minOrder = voucher.minOrderPrice.toString(),
                totalQuantity = voucher.quantity.toString(),
                maxUsagePerUser = it.maxUsagePerUser.ifBlank { "1" },
                validFrom = parseApiDateToUi(voucher.validFrom),
                validTo = parseApiDateToUi(voucher.validTo)
            )
        }
        _isEditSheetVisible.value = true
    }

    fun onDismissEditSheet() {
        _isEditSheetVisible.value = false
        _editingVoucherId.value = null
    }

    fun onDeleteIconClick(voucherId: String) {
        _voucherToDelete.value = voucherId
    }

    fun onDismissDeleteDialog() {
        _voucherToDelete.value = null
    }

    fun onClearExpiredClick() {
        _showDeleteExpiredDialog.value = true
    }

    fun onDismissDeleteExpiredDialog() {
        _showDeleteExpiredDialog.value = false
    }

    fun confirmDeleteExpiredVouchers() {
        viewModelScope.launch {
            val result = repository.deleteExpiredVouchers()
            result.onSuccess {
                _uiState.update { it.copy(bannerMessage = "Expired vouchers cleared", isBannerSuccess = true, showBanner = true) }
                _showDeleteExpiredDialog.value = false
                loadVouchers(true)
            }.onFailure { error ->
                showErrorBanner(error.message ?: "Unable to clear expired vouchers.")
            }
        }
    }

    fun confirmDeleteVoucher() {
        viewModelScope.launch {
            if (_voucherToDelete.value == null) return@launch
            val result = repository.deleteVoucher(_voucherToDelete.value!!)
            result.onSuccess {
                _uiState.update { it.copy(bannerMessage = "Voucher deleted.", isBannerSuccess = true, showBanner = true) }
                _voucherToDelete.value = null
                loadVouchers(true)
            }.onFailure { error ->
                showErrorBanner(error.message ?: "Unable to delete voucher.")
            }
        }
    }

    fun onUpdateVoucherClick() {
        val state = _uiState.value
        val voucherId = _editingVoucherId.value ?: return
        if (state.isLoading) return

        if (!validateVoucherFields(state)) return

        val discount = state.discountValue.trim().toDouble()
        val minOrder = state.minOrder.trim().toDouble()
        val totalQuantity = state.totalQuantity.trim().toInt()
        val maxUsagePerUser = state.maxUsagePerUser.trim().toInt()
        val maxPriceDiscount = if (state.discountStyle == 2) state.maxReduction.trim().toDouble() else 0.0

        val formattedValidFrom = parseDateToIso(state.validFrom, endOfDay = false)
        val formattedValidTo = parseDateToIso(state.validTo, endOfDay = true)

        if (formattedValidFrom == null || formattedValidTo == null) {
            showErrorBanner("Invalid date format. Please use MM/dd/yyyy or yyyy-MM-dd.")
            return
        }

        val dto = UpdateVoucherDto(
            voucherDescription = state.description.trim(),
            voucherScope = state.targetApplication,
            discountType = state.discountStyle,
            discount = discount,
            maxPriceDiscount = maxPriceDiscount,
            minOrderPrice = minOrder,
            totalQuantity = totalQuantity,
            maxUsagePerUser = maxUsagePerUser,
            validFrom = formattedValidFrom,
            validTo = formattedValidTo
        )

        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val result = repository.updateVoucher(voucherId, dto)
            _uiState.update { it.copy(isLoading = false) }
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        bannerMessage = "Voucher updated.",
                        isBannerSuccess = true,
                        showBanner = true,
                        voucherName = "", description = "", discountValue = "",
                        maxReduction = "", minOrder = "", totalQuantity = "",
                        maxUsagePerUser = "", validFrom = "", validTo = ""
                    )
                }
                _isEditSheetVisible.value = false
                _editingVoucherId.value = null
                loadVouchers(true)
            }.onFailure { error ->
                showErrorBanner(error.message ?: "Action failed.")
            }
        }
    }

    // ==================
    // CÁC HÀM PHỤ TRỢ
    // ==================

    private fun validateVoucherFields(state: VoucherUiState): Boolean {
        if (state.validFrom.isBlank() || state.validTo.isBlank()) {
            showErrorBanner("Please select valid dates.")
            return false
        }
        val discount = state.discountValue.trim().toDoubleOrNull()
        if (discount == null || discount <= 0.0) {
            showErrorBanner("Discount value is invalid.")
            return false
        }
        val minOrder = state.minOrder.trim().toDoubleOrNull()
        if (minOrder == null || minOrder < 0.0) {
            showErrorBanner("Minimum order value is invalid.")
            return false
        }
        val totalQuantity = state.totalQuantity.trim().toIntOrNull()
        if (totalQuantity == null || totalQuantity <= 0) {
            showErrorBanner("Total quantity is invalid.")
            return false
        }
        val maxUsage = state.maxUsagePerUser.trim().toIntOrNull()
        if (maxUsage == null || maxUsage <= 0) {
            showErrorBanner("Max usage per user is invalid.")
            return false
        }
        if (state.discountStyle == 2) {
            val maxRed = state.maxReduction.trim().toDoubleOrNull()
            if (maxRed == null || maxRed <= 0.0) {
                showErrorBanner("Max reduction is invalid.")
                return false
            }
        }
        return true
    }

    private fun showErrorBanner(message: String) {
        _uiState.update { it.copy(bannerMessage = message, isBannerSuccess = false, showBanner = true) }
    }

    private fun parseDateToIso(raw: String?, endOfDay: Boolean): String? {
        val input = raw?.trim().orEmpty()
        if (input.isEmpty()) return null

        val patterns = listOf(
            DATE_FORMAT_YYYY_MM_DD,
            "MM/dd/yyyy",
            "dd/MM/yyyy",
            "yyyy/MM/dd",
            "M/d/yyyy",
            "d/M/yyyy",
            "dd-MM-yyyy"
        )
        val parsed = patterns.firstNotNullOfOrNull { pattern ->
            val sdf = SimpleDateFormat(pattern, Locale.US).apply { isLenient = false }
            try {
                sdf.parse(input)
            } catch (_: ParseException) {
                null
            }
        } ?: return null

        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US).apply {
            time = parsed
            set(Calendar.HOUR_OF_DAY, if (endOfDay) 23 else 0)
            set(Calendar.MINUTE, if (endOfDay) 59 else 0)
            set(Calendar.SECOND, if (endOfDay) 59 else 0)
            set(Calendar.MILLISECOND, 0)
        }

        val outFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return outFormat.format(calendar.time)
    }

    private fun parseApiDateToUi(raw: String?): String {
        val input = raw?.trim().orEmpty()
        if (input.isEmpty()) return ""

        val inputPatterns = listOf(
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss",
            DATE_FORMAT_YYYY_MM_DD
        )
        val parsed = inputPatterns.firstNotNullOfOrNull { pattern ->
            val sdf = SimpleDateFormat(pattern, Locale.US).apply { isLenient = false }
            try {
                sdf.parse(input)
            } catch (_: ParseException) {
                null
            }
        } ?: return input

        val outFormat = SimpleDateFormat(DATE_FORMAT_YYYY_MM_DD, Locale.US)
        return outFormat.format(parsed)
    }

    private fun mapVoucherScopeToUi(scope: String?): Int {
        return when (scope?.lowercase(Locale.US)) {
            "shipping" -> 2
            else -> 1
        }
    }

    private fun mapDiscountTypeToUi(type: String?): Int {
        return when (type?.lowercase(Locale.US)) {
            "percentage" -> 2
            else -> 1
        }
    }

    companion object {
        private const val DEFAULT_PAGE_SIZE = 10
        private const val DATE_FORMAT_YYYY_MM_DD = "yyyy-MM-dd"
    }
}