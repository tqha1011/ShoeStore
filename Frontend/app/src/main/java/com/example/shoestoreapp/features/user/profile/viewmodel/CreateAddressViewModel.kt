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

    // --- BIẾN TRẠNG THÁI CHẾ ĐỘ SỬA/THÊM ---
    private var currentAddressId: String? = null
    val isEditMode: Boolean get() = currentAddressId != null

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

    // Banner properties
    private val _bannerMessage = MutableStateFlow("")
    val bannerMessage = _bannerMessage.asStateFlow()

    private val _isBannerSuccess = MutableStateFlow(true)
    val isBannerSuccess = _isBannerSuccess.asStateFlow()

    private val _showBanner = MutableStateFlow(false)
    val showBanner = _showBanner.asStateFlow()

    init {
        fetchProvinces()
    }

    // --- HÀM KHỞI TẠO DỮ LIỆU TỪ MÀN HÌNH ---
    fun initData(addressId: String?) {
        if (addressId != null && currentAddressId != addressId) {
            currentAddressId = addressId
            loadAddressForEdit(addressId)
        }
    }

    private fun loadAddressForEdit(addressId: String) {
        viewModelScope.launch {
            _uiState.value = CreateAddressUiState.Loading

            val addresses = addressRepository.getAllAddresses().getOrNull() ?: emptyList()
            val addressDto = addresses.find { it.id == addressId }

            if (addressDto != null) {
                _isDefault.value = addressDto.isDefault

                parseAddressString(addressDto.address)

                syncAdministrativeData()
            }
            _uiState.value = CreateAddressUiState.Idle
        }
    }

    // --- HÀM PHỤ TRỢ 1: CHUYÊN XẺ CHUỖI ĐỊA CHỈ ---
    private fun parseAddressString(fullAddress: String) {
        val parts = fullAddress.split(",").map { it.trim() }

        if (parts.size >= 4) {
            _selectedCity.value = parts[parts.size - 1]
            _selectedDistrict.value = parts[parts.size - 2]
            _selectedWard.value = parts[parts.size - 3]
            _exactAddress.value = parts.dropLast(3).joinToString(", ")
        } else if (parts.size == 3) {
            _selectedCity.value = parts[parts.size - 1]
            _selectedDistrict.value = parts[parts.size - 2]
            _selectedWard.value = ""
            _exactAddress.value = parts[0]
        } else {
            _exactAddress.value = fullAddress
        }
    }

    // --- HÀM PHỤ TRỢ 2: CHUYÊN ĐỒNG BỘ DỮ LIỆU TỈNH/HUYỆN/XÃ VỚI API ---
    private suspend fun syncAdministrativeData() {
        val provList = repository.getProvinces().getOrNull() ?: emptyList()
        _provinces.value = provList
        val prov = provList.find { it.name.equals(_selectedCity.value, ignoreCase = true) }

        if (prov == null) return
        _selectedProvinceId.value = prov.code

        val distList = repository.getDistricts(prov.code).getOrNull() ?: emptyList()
        _districts.value = distList
        val dist = distList.find { it.name.equals(_selectedDistrict.value, ignoreCase = true) }

        if (dist == null) return
        _selectedDistrictId.value = dist.code

        val wardList = repository.getWards(dist.code).getOrNull() ?: emptyList()
        _wards.value = wardList
        val wrd = wardList.find { it.name.equals(_selectedWard.value, ignoreCase = true) }

        if (wrd != null) {
            _selectedWardId.value = wrd.code
        }
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

    fun hideBanner() {
        _showBanner.value = false
    }

    // --- HÀM LƯU TỰ ĐỘNG NHẬN DIỆN THÊM MỚI HAY CẬP NHẬT ---
    fun saveAddress() {
        val provinceName = _selectedCity.value.trim()
        val districtName = _selectedDistrict.value.trim()
        val wardName = _selectedWard.value.trim()
        val detailAddress = _exactAddress.value.trim()

        if (provinceName.isBlank() || districtName.isBlank() || wardName.isBlank() || detailAddress.isBlank()) {
            _bannerMessage.value = "Please complete all address fields."
            _isBannerSuccess.value = false
            _showBanner.value = true
            return
        }

        _uiState.value = CreateAddressUiState.Loading

        viewModelScope.launch {
            val dto = CreateAddressDto(
                detailAddress = detailAddress,
                district = districtName,
                isDefault = _isDefault.value,
                province = provinceName,
                ward = wardName
            )

            val result = if (isEditMode) {
                addressRepository.updateAddress(currentAddressId!!, dto)
            } else {
                addressRepository.createAddress(dto)
            }

            result.fold(
                onSuccess = {
                    _bannerMessage.value = if (isEditMode) "Address updated successfully" else "Address created successfully"
                    _isBannerSuccess.value = true
                    _showBanner.value = true
                    _uiState.value = CreateAddressUiState.Success
                },
                onFailure = { error ->
                    val errorMsg = error.message ?: "Failed to save address."
                    _bannerMessage.value = errorMsg
                    _isBannerSuccess.value = false
                    _showBanner.value = true
                    _uiState.value = CreateAddressUiState.Error(errorMsg)
                }
            )
        }
    }
}