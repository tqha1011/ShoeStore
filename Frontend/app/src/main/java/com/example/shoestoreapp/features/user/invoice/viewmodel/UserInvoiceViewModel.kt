package com.example.shoestoreapp.features.user.invoice.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestoreapp.features.invoice.model.Detail
import com.example.shoestoreapp.features.invoice.model.Invoice
import com.example.shoestoreapp.features.invoice.model.InvoiceStatus
import com.example.shoestoreapp.features.user.invoice.data.UserInvoiceRepository
import kotlinx.coroutines.launch

data class UserInvoiceState(
    // Controls full-screen loading for initial invoice fetch.
    val isLoading: Boolean = false,
    // Source list returned from API.
    val invoices: List<Invoice> = emptyList(),
    // Active chip filter on the user invoice screen.
    val selectedStatus: InvoiceStatus? = null,
    // Badge counts shown in profile/order shortcuts.
    val orderCounts: Map<InvoiceStatus, Int> = InvoiceStatus.entries.associateWith { 0 },
    // One-shot error message for snackbar.
    val error: String? = null,
    // One-shot success message for snackbar.
    val successMessage: String? = null,
    // Invoice currently opened in details sheet.
    val selectedInvoice: Invoice? = null,
    // Line items of selected invoice.
    val invoiceDetails: List<Detail> = emptyList(),
    // Controls details bottom-sheet loading state.
    val isDetailLoading: Boolean = false,
    // True while cancel request is in progress.
    val isCancelling: Boolean = false,
    // Tracks exactly which card is cancelling.
    val cancellingInvoicePublicId: String? = null
)

class UserInvoiceViewModel(
    private val repository: UserInvoiceRepository
) : ViewModel() {

    var state by mutableStateOf(UserInvoiceState())
        private set

    init {
        loadInvoices()
    }

    fun loadInvoices() {
        viewModelScope.launch {
            // Start fresh loading cycle and clear old transient error.
            state = state.copy(isLoading = true, error = null)

            repository.getAllInvoicesUser()
                .onSuccess { invoices ->
                    state = state.copy(
                        isLoading = false,
                        invoices = invoices,
                        orderCounts = buildOrderCounts(invoices)
                    )
                }
                .onFailure { throwable ->
                    state = state.copy(
                        isLoading = false,
                        error = throwable.message ?: "Failed to load invoices"
                    )
                }
        }
    }

    fun applyInitialFilter(status: InvoiceStatus?) {
        // Applies nav-provided filter only when it changes.
        if (state.selectedStatus != status) {
            state = state.copy(selectedStatus = status)
        }
    }

    fun onFilterSelected(status: InvoiceStatus?) {
        state = state.copy(selectedStatus = status)
    }

    fun openInvoiceDetails(invoice: Invoice) {
        // publicId is required to fetch detail endpoint.
        val invoiceGuid = invoice.publicId.trim()
        if (invoiceGuid.isEmpty()) {
            state = state.copy(error = "Cannot open details: missing invoice id")
            return
        }

        viewModelScope.launch {
            // Open sheet immediately with loading state.
            state = state.copy(
                selectedInvoice = invoice,
                invoiceDetails = emptyList(),
                isDetailLoading = true,
                error = null
            )

            repository.getInvoiceDetailsUser(invoiceGuid)
                .onSuccess { detailsList ->
                    state = state.copy(isDetailLoading = false, invoiceDetails = detailsList)
                }
                .onFailure { throwable ->
                    state = state.copy(
                        isDetailLoading = false,
                        error = throwable.message ?: "Failed to load invoice details"
                    )
                }
        }
    }

    fun cancelInvoice(invoice: Invoice) {
        val invoiceGuid = invoice.publicId.trim()

        if (invoiceGuid.isEmpty()) {
            state = state.copy(error = "Cannot cancel order: missing invoice id")
            return
        }

        // User is only allowed to cancel pending invoices.
        if (invoice.status != InvoiceStatus.PENDING) {
            state = state.copy(error = "Only pending orders can be cancelled.")
            return
        }

        viewModelScope.launch {
            // Keep cancel loading scoped to the clicked order.
            state = state.copy(
                isCancelling = true,
                cancellingInvoicePublicId = invoiceGuid,
                error = null
            )

            repository.updateInvoiceStatusUser(invoiceGuid, "Canceled")
                .onSuccess {
                    // Sync list and selected detail after successful cancel.
                    val updatedInvoices = state.invoices.map { current ->
                        if (current.publicId == invoiceGuid) {
                            current.copy(status = InvoiceStatus.CANCELLED)
                        } else {
                            current
                        }
                    }
                    val updatedSelected = state.selectedInvoice?.let { selected ->
                        if (selected.publicId == invoiceGuid) {
                            selected.copy(status = InvoiceStatus.CANCELLED)
                        } else {
                            selected
                        }
                    }

                    state = state.copy(
                        invoices = updatedInvoices,
                        selectedInvoice = updatedSelected,
                        orderCounts = buildOrderCounts(updatedInvoices),
                        isCancelling = false,
                        cancellingInvoicePublicId = null,
                        successMessage = "Order cancelled successfully."
                    )
                }
                .onFailure { throwable ->
                    state = state.copy(
                        isCancelling = false,
                        cancellingInvoicePublicId = null,
                        error = throwable.message ?: "Cancel order failed"
                    )
                }
        }
    }


    fun clearDetails() {
        // Reset detail sheet state on dismiss.
        state = state.copy(
            selectedInvoice = null,
            invoiceDetails = emptyList(),
            isDetailLoading = false,
            isCancelling = false,
            cancellingInvoicePublicId = null
        )
    }

    fun clearTransientMessage() {
        state = state.copy(error = null, successMessage = null)
    }

    val filteredInvoices: List<Invoice>
        get() {
            // Return all invoices when no filter is selected.
            val selected = state.selectedStatus ?: return state.invoices
            return state.invoices.filter { it.status == selected }
        }

    private fun buildOrderCounts(invoices: List<Invoice>): Map<InvoiceStatus, Int> {
        return InvoiceStatus.entries.associateWith { status ->
            invoices.count { invoice -> invoice.status == status }
        }
    }
}
