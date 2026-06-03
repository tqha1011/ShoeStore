package com.example.shoestoreapp.features.user.profile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shoestoreapp.features.user.product.ui.components.TopBanner
import com.example.shoestoreapp.features.user.profile.ui.components.AddressDropdownField
import com.example.shoestoreapp.features.user.profile.viewmodel.CreateAddressUiState
import com.example.shoestoreapp.features.user.profile.viewmodel.CreateAddressViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAddressScreen(
    addressId: String? = null,
    viewModel: CreateAddressViewModel = viewModel(),
    onBackClick: () -> Unit = {}
) {
    val selectedCity by viewModel.selectedCity.collectAsState()
    val selectedDistrict by viewModel.selectedDistrict.collectAsState()
    val selectedWard by viewModel.selectedWard.collectAsState()
    val exactAddress by viewModel.exactAddress.collectAsState()
    val isDefault by viewModel.isDefault.collectAsState()
    val provinces by viewModel.provinces.collectAsState()
    val districts by viewModel.districts.collectAsState()
    val wards by viewModel.wards.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val bannerMessage by viewModel.bannerMessage.collectAsState()
    val isBannerSuccess by viewModel.isBannerSuccess.collectAsState()
    val showBanner by viewModel.showBanner.collectAsState()

    LaunchedEffect(addressId) {
        viewModel.initData(addressId)
    }

    val isEditMode = viewModel.isEditMode

    LaunchedEffect(uiState) {
        if (uiState is CreateAddressUiState.Success) {
            kotlinx.coroutines.delay(1500)
            viewModel.hideBanner()
            onBackClick()
        }
    }

    val isLoading = uiState is CreateAddressUiState.Loading
    val isFormValid = selectedCity.isNotEmpty() &&
            selectedDistrict.isNotEmpty() &&
            selectedWard.isNotEmpty() &&
            exactAddress.isNotBlank()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.Black
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            },
            containerColor = Color.White
        ) { paddingValues ->
            CreateAddressFormContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                isEditMode = isEditMode,
                selectedCity = selectedCity,
                selectedDistrict = selectedDistrict,
                selectedWard = selectedWard,
                exactAddress = exactAddress,
                isDefault = isDefault,
                provinces = provinces.map { it.name },
                districts = districts.map { it.name },
                wards = wards.map { it.name },
                isFormValid = isFormValid,
                isLoading = isLoading,
                onCitySelected = viewModel::onCitySelected,
                onDistrictSelected = viewModel::onDistrictSelected,
                onWardSelected = viewModel::onWardSelected,
                onExactAddressChanged = viewModel::onExactAddressChanged,
                onDefaultChanged = viewModel::onDefaultChanged,
                onSaveClicked = viewModel::saveAddress
            )
        }

        Box(modifier = Modifier.align(Alignment.TopCenter)) {
            TopBanner(
                message = bannerMessage,
                isSuccess = isBannerSuccess,
                isVisible = showBanner,
                onDismiss = { viewModel.hideBanner() }
            )
        }
    }
}

@Composable
fun CreateAddressFormContent(
    modifier: Modifier = Modifier,
    isEditMode: Boolean,
    selectedCity: String,
    selectedDistrict: String,
    selectedWard: String,
    exactAddress: String,
    isDefault: Boolean,
    provinces: List<String>,
    districts: List<String>,
    wards: List<String>,
    isFormValid: Boolean,
    isLoading: Boolean,
    onCitySelected: (String) -> Unit,
    onDistrictSelected: (String) -> Unit,
    onWardSelected: (String) -> Unit,
    onExactAddressChanged: (String) -> Unit,
    onDefaultChanged: (Boolean) -> Unit,
    onSaveClicked: () -> Unit
) {
    Column(
        modifier = modifier
            .background(Color.White)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = if (isEditMode) "EDIT ADDRESS" else "ADD ADDRESS",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isEditMode) "Update your delivery location" else "Create a new delivery location",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF4C4546)
            )
        }

        // 1. City / Province
        AddressDropdownField(
            label = "City / Province",
            selectedValue = selectedCity,
            options = provinces,
            onOptionSelected = onCitySelected
        )

        // 2. District
        AddressDropdownField(
            label = "District",
            selectedValue = selectedDistrict,
            options = districts,
            onOptionSelected = onDistrictSelected,
            enabled = selectedCity.isNotEmpty()
        )

        // 3. Ward / Commune
        AddressDropdownField(
            label = "Ward / Commune",
            selectedValue = selectedWard,
            options = wards,
            onOptionSelected = onWardSelected,
            enabled = selectedDistrict.isNotEmpty()
        )

        // 4. Street Address Details
        OutlinedTextField(
            value = exactAddress,
            onValueChange = onExactAddressChanged,
            label = { Text("Street Address Details") },
            placeholder = { Text("e.g. 123 Air Max Boulevard, Apt 4B") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color(0xFFE5E7EB),
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.Gray
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // 5. Default Address Configuration
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isDefault,
                onClick = { onDefaultChanged(!isDefault) },
                colors = RadioButtonDefaults.colors(
                    selectedColor = Color.Black,
                    unselectedColor = Color.Gray
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Set as default delivery address",
                fontSize = 14.sp,
                color = Color.Black,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 6. Submit Button
        Button(
            onClick = onSaveClicked,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
            shape = RoundedCornerShape(50),
            enabled = isFormValid && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = if (isEditMode) "UPDATE ADDRESS" else "SAVE ADDRESS",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}