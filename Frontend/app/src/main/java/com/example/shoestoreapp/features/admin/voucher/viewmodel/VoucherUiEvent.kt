package com.example.shoestoreapp.features.admin.voucher.viewmodel

sealed interface VoucherUiEvent {
    data class ShowSuccess(val message: String) : VoucherUiEvent
    data class ShowError(val message: String) : VoucherUiEvent
}

