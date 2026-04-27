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
    // Controls first-load spinner on admin invoice screen.
    val isLoading: Boolean = false,
    // Source invoices returned from backend.
    val invoices: List<Invoice> = emptyList(),
    // One-shot error message for snackbar.
    val error: String? = null,
    // One-shot success message for snackbar.
    val successMessage: String? = null,
    // Controls detail bottom sheet loading state.
    val isDetailLoading: Boolean = false,
    // Invoice currently opened in detail sheet.
    val selectedInvoice: Invoice? = null,
    // Detailed product lines for selected invoice.
    val invoiceDetails: List<Detail> = emptyList(),
    // True while status update request is running.
    val isUpdatingStatus: Boolean = false,
    // Tracks invoice being updated for button-level loading.
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
            // Refreshes admin invoice list and clears transient error.
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
        // publicId is required by the details endpoint.
        val invoiceGuid = invoice.publicId.trim()
        if (invoiceGuid.isEmpty()) {
            state = state.copy(error = "Cannot open details: missing invoice id")
            return
        }

        viewModelScope.launch {
            // Open sheet with loading state while details are fetched.
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
        // publicId is required by the update endpoint.
        val invoiceGuid = invoice.publicId.trim()
        if (invoiceGuid.isEmpty()) {
            state = state.copy(error = "Cannot update status: missing invoice id")
            return
        }

        // Prevent unnecessary update request.
        if (invoice.status == targetStatus) {
            state = state.copy(successMessage = "Order is already ${targetStatus.name.lowercase()}.")
            return
        }

        // Enforces one-step workflow transitions.
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
            // Marks exactly which row is updating.
            state = state.copy(
                isUpdatingStatus = true,
                updatingInvoicePublicId = invoiceGuid,
                error = null
            )

            repository.updateInvoiceStatusAdmin(invoiceGuid, targetStatus.displayName())
                .onSuccess {
                    // Sync both list and selected detail after update.
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