package com.example.shoestoreapp.features.user.profile.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shoestoreapp.features.user.profile.ui.components.AddressDropdownField
import com.example.shoestoreapp.features.user.profile.viewmodel.CreateAddressUiState
import com.example.shoestoreapp.features.user.profile.viewmodel.CreateAddressViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAddressScreen(
    viewModel: CreateAddressViewModel = viewModel(),
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val selectedCity by viewModel.selectedCity.collectAsState()
    val selectedDistrict by viewModel.selectedDistrict.collectAsState()
    val selectedWard by viewModel.selectedWard.collectAsState()
    val exactAddress by viewModel.exactAddress.collectAsState()
    val isDefault by viewModel.isDefault.collectAsState()
    val provinces by viewModel.provinces.collectAsState()
    val districts by viewModel.districts.collectAsState()
    val wards by viewModel.wards.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        when (uiState) {
            is CreateAddressUiState.Success -> {
                Toast.makeText(context, "Address created successfully", Toast.LENGTH_SHORT).show()
                onBackClick()
            }
            is CreateAddressUiState.Error -> {
                val message = (uiState as CreateAddressUiState.Error).message
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
            else -> Unit
        }
    }

    val isLoading = uiState is CreateAddressUiState.Loading
    val isFormValid = selectedCity.isNotEmpty() &&
            selectedDistrict.isNotEmpty() &&
            selectedWard.isNotEmpty() &&
            exactAddress.isNotBlank()

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Phần tiêu đề màn hình
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Text(
                    text = "ADD ADDRESS",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1).sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Create a new delivery location",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF4C4546)
                )
            }

            // 1. Ô lựa chọn Thành phố / Tỉnh
            AddressDropdownField(
                label = "City / Province",
                selectedValue = selectedCity,
                options = provinces.map { it.name },
                onOptionSelected = { viewModel.onCitySelected(it) }
            )

            // 2. Ô lựa chọn Quận / Huyện (Chỉ kích hoạt mở khóa khi cấp trên đã được chọn)
            AddressDropdownField(
                label = "District",
                selectedValue = selectedDistrict,
                options = districts.map { it.name },
                onOptionSelected = { viewModel.onDistrictSelected(it) },
                enabled = selectedCity.isNotEmpty()
            )

            // 3. Ô lựa chọn Phường / Xã (Chỉ kích hoạt mở khóa khi cấp trên đã được chọn)
            AddressDropdownField(
                label = "Ward / Commune",
                selectedValue = selectedWard,
                options = wards.map { it.name },
                onOptionSelected = { viewModel.onWardSelected(it) },
                enabled = selectedDistrict.isNotEmpty()
            )

            // 4. Ô nhập văn bản địa chỉ chính xác chi tiết
            OutlinedTextField(
                value = exactAddress,
                onValueChange = { viewModel.onExactAddressChanged(it) },
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

            // 5. Cụm lựa chọn cấu hình địa chỉ mặc định
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isDefault,
                    onClick = { viewModel.onDefaultChanged(!isDefault) },
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

            // 6. Nút xác nhận thực thi
            Button(
                onClick = { viewModel.saveAddress() },
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
                        text = "SAVE ADDRESS",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}