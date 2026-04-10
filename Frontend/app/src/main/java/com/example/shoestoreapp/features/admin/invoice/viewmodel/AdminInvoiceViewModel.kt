package com.example.shoestoreapp.features.admin.invoice.viewmodel

import androidx.lifecycle.ViewModel
import com.example.shoestoreapp.features.admin.invoice.data.AdminInvoiceMockRepository
import com.example.shoestoreapp.features.invoice.model.Invoice
import com.example.shoestoreapp.features.invoice.model.InvoiceStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AdminInvoiceViewModel(
    private val repository: AdminInvoiceMockRepository = AdminInvoiceMockRepository()
) : ViewModel() {

    private val _allInvoices = MutableStateFlow(repository.getInvoices())
    private val _selectedStatus = MutableStateFlow<InvoiceStatus?>(null)
    private val _visibleInvoices = MutableStateFlow(_allInvoices.value)

    val selectedStatus: StateFlow<InvoiceStatus?> = _selectedStatus.asStateFlow()
    val visibleInvoices: StateFlow<List<Invoice>> = _visibleInvoices.asStateFlow()

    fun onFilterChange(status: InvoiceStatus?) {
        _selectedStatus.value = status
        applyFilter()
    }

    fun cycleStatus(invoiceId: Int) {
        _allInvoices.update { invoices ->
            invoices.map { invoice ->
                if (invoice.id != invoiceId) return@map invoice

                val next = when (invoice.status) {
                    InvoiceStatus.PENDING -> InvoiceStatus.PAID
                    InvoiceStatus.PAID -> InvoiceStatus.DELIVERING
                    InvoiceStatus.DELIVERING -> InvoiceStatus.CANCELED
                    InvoiceStatus.CANCELED -> InvoiceStatus.PENDING
                }
                invoice.copy(status = next)
            }
        }
        applyFilter()
    }

    private fun applyFilter() {
        val status = _selectedStatus.value
        _visibleInvoices.value = if (status == null) {
            _allInvoices.value
        } else {
            _allInvoices.value.filter { it.status == status }
        }
    }
}

