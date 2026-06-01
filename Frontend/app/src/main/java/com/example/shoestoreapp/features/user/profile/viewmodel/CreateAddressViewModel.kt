package com.example.shoestoreapp.features.user.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestoreapp.features.user.profile.data.remote.CreateAddressDto
import com.example.shoestoreapp.features.user.profile.data.remote.DistrictDto
import com.example.shoestoreapp.features.user.profile.data.remote.ProvinceDto
import com.example.shoestoreapp.features.user.profile.data.remote.WardDto
import com.example.shoestoreapp.features.user.profile.data.repositories.AddressRepository
import com.example.shoestoreapp.features.user.profile.data.repositories.AddressRepositoryImpl
import com.example.shoestoreapp.features.user.profile.data.repositories.AdministrativeRepository
import com.example.shoestoreapp.features.user.profile.data.repositories.AdministrativeRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface CreateAddressUiState {
    data object Idle : CreateAddressUiState
    data object Loading : CreateAddressUiState
    data object Success : CreateAddressUiState
    data class Error(val message: String) : CreateAddressUiState
}

class CreateAddressViewModel(
    private val repository: AdministrativeRepository = AdministrativeRepositoryImpl(),
    private val addressRepository: AddressRepository = AddressRepositoryImpl()
) : ViewModel() {

    // Luồng dữ liệu theo dõi giá trị nhập liệu của người dùng
    private val _selectedCity = MutableStateFlow("")
    val selectedCity = _selectedCity.asStateFlow()

    private val _selectedDistrict = MutableStateFlow("")
    val selectedDistrict = _selectedDistrict.asStateFlow()

    private val _selectedWard = MutableStateFlow("")
    val selectedWard = _selectedWard.asStateFlow()

    private val _exactAddress = MutableStateFlow("")
    val exactAddress = _exactAddress.asStateFlow()

    private val _isDefault = MutableStateFlow(false)
    val isDefault = _isDefault.asStateFlow()

    private val _provinces = MutableStateFlow<List<ProvinceDto>>(emptyList())
    val provinces: StateFlow<List<ProvinceDto>> = _provinces.asStateFlow()

    private val _districts = MutableStateFlow<List<DistrictDto>>(emptyList())
    val districts: StateFlow<List<DistrictDto>> = _districts.asStateFlow()

    private val _wards = MutableStateFlow<List<WardDto>>(emptyList())
    val wards: StateFlow<List<WardDto>> = _wards.asStateFlow()

    private val _selectedProvinceId = MutableStateFlow<Int?>(null)
    private val _selectedDistrictId = MutableStateFlow<Int?>(null)
    private val _selectedWardId = MutableStateFlow<Int?>(null)

    private val _uiState = MutableStateFlow<CreateAddressUiState>(CreateAddressUiState.Idle)
    val uiState: StateFlow<CreateAddressUiState> = _uiState.asStateFlow()

    init {
        fetchProvinces()
    }

    private fun fetchProvinces() {
        viewModelScope.launch {
            repository.getProvinces()
                .onSuccess { _provinces.value = it }
                .onFailure { _provinces.value = emptyList() }
        }
    }

    private fun fetchDistricts(provinceCode: Int) {
        viewModelScope.launch {
            repository.getDistricts(provinceCode)
                .onSuccess { _districts.value = it }
                .onFailure { _districts.value = emptyList() }
        }
    }

    private fun fetchWards(districtCode: Int) {
        viewModelScope.launch {
            repository.getWards(districtCode)
                .onSuccess { _wards.value = it }
                .onFailure { _wards.value = emptyList() }
        }
    }

    fun onCitySelected(city: String) {
        _selectedCity.value = city
        _selectedDistrict.value = ""
        _selectedWard.value = ""
        _districts.value = emptyList()
        _wards.value = emptyList()

        val province = _provinces.value.firstOrNull { it.name == city }
        _selectedProvinceId.value = province?.code
        _selectedDistrictId.value = null
        _selectedWardId.value = null

        if (province != null) {
            fetchDistricts(province.code)
        }
    }

    fun onDistrictSelected(district: String) {
        _selectedDistrict.value = district
        _selectedWard.value = ""
        _wards.value = emptyList()

        val districtDto = _districts.value.firstOrNull { it.name == district }
        _selectedDistrictId.value = districtDto?.code
        _selectedWardId.value = null

        if (districtDto != null) {
            fetchWards(districtDto.code)
        }
    }

    fun onWardSelected(ward: String) {
        _selectedWard.value = ward
        val wardDto = _wards.value.firstOrNull { it.name == ward }
        _selectedWardId.value = wardDto?.code
    }

    fun onExactAddressChanged(address: String) {
        _exactAddress.value = address
    }

    fun onDefaultChanged(isDefault: Boolean) {
        _isDefault.value = isDefault
    }

    fun saveAddress() {
        val provinceId = _selectedProvinceId.value
        val districtId = _selectedDistrictId.value
        val wardId = _selectedWardId.value
        val detailAddress = _exactAddress.value.trim()

        if (provinceId == null || districtId == null || wardId == null || detailAddress.isBlank()) {
            _uiState.value = CreateAddressUiState.Error("Please complete all address fields.")
            return
        }

        _uiState.value = CreateAddressUiState.Loading
        viewModelScope.launch {
            val dto = CreateAddressDto(
                detailAddress = detailAddress,
                districtId = districtId,
                isDefault = _isDefault.value,
                provinceId = provinceId,
                wardId = wardId
            )

            addressRepository.createAddress(dto)
                .onSuccess { _uiState.value = CreateAddressUiState.Success }
                .onFailure { error ->
                    _uiState.value = CreateAddressUiState.Error(
                        error.message ?: "Failed to create address."
                    )
                }
        }
    }
}