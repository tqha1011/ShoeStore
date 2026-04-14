package com.example.shoestoreapp.features.user.invoice.viewmodel

import androidx.lifecycle.ViewModel
import com.example.shoestoreapp.features.invoice.model.Invoice
import com.example.shoestoreapp.features.invoice.model.InvoiceStatus
import com.example.shoestoreapp.features.user.invoice.data.UserInvoiceMockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserInvoiceViewModel(
    private val repository: UserInvoiceMockRepository = UserInvoiceMockRepository(),
    userId: Int = 1
) : ViewModel() {

    private val userInvoices = repository.getInvoicesByUser(userId)
    private val _selectedStatus = MutableStateFlow<InvoiceStatus?>(null)
    private val _visibleInvoices = MutableStateFlow(userInvoices)

    val selectedStatus: StateFlow<InvoiceStatus?> = _selectedStatus.asStateFlow()
    val visibleInvoices: StateFlow<List<Invoice>> = _visibleInvoices.asStateFlow()

    fun onFilterChange(status: InvoiceStatus?) {
        _selectedStatus.value = status
        _visibleInvoices.value = if (status == null) {
            userInvoices
        } else {
            userInvoices.filter { it.status == status }
        }
    }
}

