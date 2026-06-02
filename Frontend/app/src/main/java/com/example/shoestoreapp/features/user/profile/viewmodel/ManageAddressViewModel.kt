package com.example.shoestoreapp.features.user.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestoreapp.features.user.profile.data.models.AddressUiModel
import com.example.shoestoreapp.features.user.profile.data.remote.CreateAddressDto
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

    // --- CÁC BIẾN BANNER ---
    private val _bannerMessage = MutableStateFlow("")
    val bannerMessage = _bannerMessage.asStateFlow()

    private val _isBannerSuccess = MutableStateFlow(true)
    val isBannerSuccess = _isBannerSuccess.asStateFlow()

    private val _showBanner = MutableStateFlow(false)
    val showBanner = _showBanner.asStateFlow()

    fun hideBanner() {
        _showBanner.value = false
    }
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
        val currentState = _uiState.value
        if (currentState !is ManageAddressUiState.Success) return

        _uiState.value = ManageAddressUiState.Loading

        viewModelScope.launch {
            repository.deleteAddress(id)
                .onSuccess {
                    _bannerMessage.value = "Address deleted successfully"
                    _isBannerSuccess.value = true
                    _showBanner.value = true
                    fetchAddresses()
                }
                .onFailure { error ->
                    _bannerMessage.value = error.message ?: "Failed to delete address"
                    _isBannerSuccess.value = false
                    _showBanner.value = true

                    _uiState.value = ManageAddressUiState.Success(currentState.addresses)
                }
        }
    }

    fun setAsDefault(id: String) {
        val currentState = _uiState.value
        if (currentState !is ManageAddressUiState.Success) return

        val targetAddress = currentState.addresses.find { it.id == id } ?: return

        viewModelScope.launch {
            val parts = targetAddress.fullAddress.split(",").map { it.trim() }

            var provinceName = ""
            var districtName = ""
            var wardName = ""
            var detailAddress = ""

            if (parts.size >= 4) {
                provinceName = parts[parts.size - 1]
                districtName = parts[parts.size - 2]
                wardName = parts[parts.size - 3]
                detailAddress = parts.dropLast(3).joinToString(", ")
            } else if (parts.size == 3) {
                provinceName = parts[parts.size - 1]
                districtName = parts[parts.size - 2]
                detailAddress = parts[0]
            } else {
                detailAddress = targetAddress.fullAddress
            }

            val dto = CreateAddressDto(
                detailAddress = detailAddress,
                district = districtName,
                isDefault = true,
                province = provinceName,
                ward = wardName
            )

            repository.updateAddress(id, dto)
                .onSuccess {
                    _bannerMessage.value = "Set default address successfully"
                    _isBannerSuccess.value = true
                    _showBanner.value = true
                    fetchAddresses()
                }
                .onFailure { error ->
                    _bannerMessage.value = error.message ?: "Failed to set default address"
                    _isBannerSuccess.value = false
                    _showBanner.value = true
                    _uiState.value = ManageAddressUiState.Success(currentState.addresses)
                }
        }
    }
}