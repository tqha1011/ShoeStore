package com.example.shoestoreapp.features.admin.invoice.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestoreapp.features.admin.invoice.data.AdminInvoiceRepository
import com.example.shoestoreapp.features.invoice.model.Detail
import com.example.shoestoreapp.features.invoice.model.Invoice
import com.example.shoestoreapp.features.invoice.model.InvoiceStatus
import com.example.shoestoreapp.features.invoice.model.displayName
import com.example.shoestoreapp.features.invoice.model.nextWorkflowStatus
import kotlinx.coroutines.launch

data class AdminInvoiceState(
    val isLoading: Boolean = false,
    val invoices: List<Invoice> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null,
    val isDetailLoading: Boolean = false,
    val selectedInvoice: Invoice? = null,
    val invoiceDetails: List<Detail> = emptyList(),
    val isUpdatingStatus: Boolean = false,
    val updatingInvoicePublicId: String? = null
)

class AdminInvoiceViewmodel (private val repository: AdminInvoiceRepository)
    : ViewModel() {
    var state by mutableStateOf(AdminInvoiceState())
        private set

    init {
        loadInvoices()
    }
    fun loadInvoices(){
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)
            repository.getAllInvoicesAdmin()
                .onSuccess { list ->
                    state = state.copy(isLoading = false, invoices = list)
                }
                .onFailure { exception ->
                    state = state.copy(isLoading = false, error = exception.message)
                }
        }
    }

    fun openInvoiceDetails(invoice: Invoice) {
        val invoiceGuid = invoice.publicId.trim()
        if (invoiceGuid.isEmpty()) {
            state = state.copy(error = "Cannot open details: missing invoice id")
            return
        }

        viewModelScope.launch {
            state = state.copy(
                selectedInvoice = invoice,
                invoiceDetails = emptyList(),
                isDetailLoading = true,
                error = null
            )

            repository.getInvoiceDetailsAdmin(invoiceGuid)
                .onSuccess { detailsList ->
                    state = state.copy(isDetailLoading = false, invoiceDetails = detailsList)
                }
                .onFailure { exception ->
                    state = state.copy(isDetailLoading = false, error = exception.message)
                }
        }
    }

    fun updateInvoiceStatus(invoice: Invoice, targetStatus: InvoiceStatus) {
        val invoiceGuid = invoice.publicId.trim()
        if (invoiceGuid.isEmpty()) {
            state = state.copy(error = "Cannot update status: missing invoice id")
            return
        }

        if (invoice.status == targetStatus) {
            state = state.copy(successMessage = "Order is already ${targetStatus.name.lowercase()}.")
            return
        }

        val nextStatus = invoice.nextWorkflowStatus()
        if (nextStatus == null || targetStatus != nextStatus) {
            val message = if (nextStatus == null) {
                "Invalid transition. This order cannot be updated manually at its current state."
            } else {
                "Invalid transition. Next status must be ${nextStatus.name}."
            }
            state = state.copy(error = message)
            return
        }

        viewModelScope.launch {
            state = state.copy(
                isUpdatingStatus = true,
                updatingInvoicePublicId = invoiceGuid,
                error = null
            )

            repository.updateInvoiceStatusAdmin(invoiceGuid, targetStatus.displayName())
                .onSuccess {
                    val updatedInvoices = state.invoices.map { current ->
                        if (current.publicId == invoiceGuid) current.copy(status = targetStatus) else current
                    }
                    val updatedSelectedInvoice = state.selectedInvoice?.let { selected ->
                        if (selected.publicId == invoiceGuid) selected.copy(status = targetStatus) else selected
                    }
                    state = state.copy(
                        invoices = updatedInvoices,
                        selectedInvoice = updatedSelectedInvoice,
                        isUpdatingStatus = false,
                        updatingInvoicePublicId = null,
                        successMessage = "Order status updated successfully"
                    )
                }
                .onFailure { exception ->
                    state = state.copy(
                        isUpdatingStatus = false,
                        updatingInvoicePublicId = null,
                        error = exception.message
                    )
                }
        }
    }

    fun clearDetails() {
        state = state.copy(
            selectedInvoice = null,
            invoiceDetails = emptyList(),
            isDetailLoading = false
        )
    }

    fun clearTransientMessage() {
        state = state.copy(error = null, successMessage = null)
    }
}