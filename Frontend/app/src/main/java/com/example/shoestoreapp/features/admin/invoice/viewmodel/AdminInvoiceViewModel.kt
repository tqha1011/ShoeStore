package com.example.shoestoreapp.features.admin.invoice.viewmodel

import androidx.lifecycle.ViewModel
import com.example.shoestoreapp.features.admin.invoice.data.AdminInvoiceMockRepository
import com.example.shoestoreapp.features.invoice.model.Invoice
import com.example.shoestoreapp.features.invoice.model.InvoiceStatus
import com.example.shoestoreapp.features.invoice.model.nextWorkflowStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AdminInvoiceViewModel(
    repository: AdminInvoiceMockRepository = AdminInvoiceMockRepository()
) : ViewModel() {

    private data class LastStatusChange(
        val orderCode: String,
        val previousStatus: InvoiceStatus
    )

    private val _allInvoices = MutableStateFlow(repository.getInvoices())
    private val _selectedStatus = MutableStateFlow<InvoiceStatus?>(null)
    private val _visibleInvoices = MutableStateFlow(_allInvoices.value)
    private var lastStatusChange: LastStatusChange? = null

    val selectedStatus: StateFlow<InvoiceStatus?> = _selectedStatus.asStateFlow()
    val visibleInvoices: StateFlow<List<Invoice>> = _visibleInvoices.asStateFlow()

    init {
        applyFilter()
    }

    fun onFilterChange(status: InvoiceStatus?) {
        _selectedStatus.value = status
        applyFilter()
    }

    fun getNextStatus(invoice: Invoice): InvoiceStatus? {
        return invoice.nextWorkflowStatus()
    }

    fun getStatusOptions(invoice: Invoice): List<InvoiceStatus> {
        val nextStatus = invoice.nextWorkflowStatus()
        val canCancel = invoice.status == InvoiceStatus.PENDING

        return buildList {
            if (nextStatus != null) add(nextStatus)
            if (canCancel) add(InvoiceStatus.CANCELED)
        }
    }

    fun advanceStatus(orderCode: String) {
        val invoice = _allInvoices.value.firstOrNull { it.orderCode == orderCode } ?: return
        val next = invoice.nextWorkflowStatus() ?: return
        updateStatus(orderCode = orderCode, targetStatus = next)
    }

    fun updateStatus(orderCode: String, targetStatus: InvoiceStatus) {
        var previousStatus: InvoiceStatus? = null

        _allInvoices.update { invoices ->
            invoices.map { invoice ->
                if (invoice.orderCode != orderCode) return@map invoice
                if (invoice.status == targetStatus) return@map invoice

                previousStatus = invoice.status
                invoice.copy(status = targetStatus)
            }
        }

        if (previousStatus != null) {
            lastStatusChange = LastStatusChange(orderCode = orderCode, previousStatus = previousStatus!!)
            applyFilter()
        }
    }

    fun undoLastStatusChange() {
        val change = lastStatusChange ?: return
        lastStatusChange = null

        _allInvoices.update { invoices ->
            invoices.map { invoice ->
                if (invoice.orderCode == change.orderCode) {
                    invoice.copy(status = change.previousStatus)
                } else {
                    invoice
                }
            }
        }
        applyFilter()
    }

    // Keep old method name to avoid breaking temporary callers.
    fun cycleStatus(orderCode: String) {
        advanceStatus(orderCode)
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

