package com.example.shoestoreapp.features.admin.invoice.viewmodel

import androidx.lifecycle.ViewModel
import com.example.shoestoreapp.features.admin.invoice.data.AdminInvoiceMockRepository
import com.example.shoestoreapp.features.invoice.model.Invoice
import com.example.shoestoreapp.features.invoice.model.InvoiceStatus
import com.example.shoestoreapp.features.invoice.model.nextWorkflowStatus
import com.example.shoestoreapp.features.invoice.model.shouldAutoCancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AdminInvoiceViewModel(
    repository: AdminInvoiceMockRepository = AdminInvoiceMockRepository()
) : ViewModel() {

    private val _allInvoices = MutableStateFlow(repository.getInvoices())
    private val _selectedStatus = MutableStateFlow<InvoiceStatus?>(null)
    private val _visibleInvoices = MutableStateFlow(_allInvoices.value)

    val selectedStatus: StateFlow<InvoiceStatus?> = _selectedStatus.asStateFlow()
    val visibleInvoices: StateFlow<List<Invoice>> = _visibleInvoices.asStateFlow()

    init {
        applyTimeoutPolicy()
        applyFilter()
    }

    fun onFilterChange(status: InvoiceStatus?) {
        applyTimeoutPolicy()
        _selectedStatus.value = status
        applyFilter()
    }

    fun getNextStatus(invoice: Invoice): InvoiceStatus? {
        return invoice.nextWorkflowStatus()
    }

    fun advanceStatus(invoiceId: Int) {
        applyTimeoutPolicy()

        _allInvoices.update { invoices ->
            invoices.map { invoice ->
                if (invoice.id != invoiceId) return@map invoice

                val next = invoice.nextWorkflowStatus()
                if (next == null) {
                    invoice
                } else {
                    invoice.copy(
                        status = next,
                        updatedAt = "Updated by admin"
                    )
                }
            }
        }
        applyFilter()
    }

    // Keep old method name to avoid breaking temporary callers.
    fun cycleStatus(invoiceId: Int) {
        advanceStatus(invoiceId)
    }

    private fun applyTimeoutPolicy() {
        _allInvoices.update { invoices ->
            invoices.map { invoice ->
                if (invoice.shouldAutoCancel()) {
                    invoice.copy(
                        status = InvoiceStatus.CANCELED,
                        updatedAt = "Auto-canceled after 30m timeout"
                    )
                } else {
                    invoice
                }
            }
        }
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

