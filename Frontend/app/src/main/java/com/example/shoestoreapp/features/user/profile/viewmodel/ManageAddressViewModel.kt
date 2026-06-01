package com.example.shoestoreapp.features.user.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestoreapp.features.user.profile.data.models.AddressUiModel
import com.example.shoestoreapp.features.user.profile.data.remote.toAddressUiModel
import com.example.shoestoreapp.features.user.profile.data.repositories.AddressRepository
import com.example.shoestoreapp.features.user.profile.data.repositories.AddressRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ManageAddressUiState {
    data object Loading : ManageAddressUiState
    data class Success(val addresses: List<AddressUiModel>) : ManageAddressUiState
    data object Empty : ManageAddressUiState
    data class Error(val message: String) : ManageAddressUiState
}

class ManageAddressViewModel(
    private val repository: AddressRepository = AddressRepositoryImpl()
) : ViewModel() {
    private val _uiState = MutableStateFlow<ManageAddressUiState>(ManageAddressUiState.Loading)
    val uiState: StateFlow<ManageAddressUiState> = _uiState.asStateFlow()

    init {
        fetchAddresses()
    }

    fun fetchAddresses() {
        _uiState.value = ManageAddressUiState.Loading
        viewModelScope.launch {
            repository.getAllAddresses()
                .onSuccess { addresses ->
                    if (addresses.isEmpty()) {
                        _uiState.value = ManageAddressUiState.Empty
                    } else {
                        _uiState.value = ManageAddressUiState.Success(
                            addresses.map { it.toAddressUiModel() }
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.value = ManageAddressUiState.Error(
                        error.message ?: "Failed to load addresses."
                    )
                }
        }
    }

    fun removeAddress(id: String) {
        // TODO: Handle remove logic
    }

    fun setAsDefault(id: String) {
        // TODO: Handle set default logic
    }
}