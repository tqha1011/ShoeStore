package com.example.shoestoreapp.features.user.profile.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.shoestoreapp.features.user.product.ui.components.UserTopBarTitle
import com.example.shoestoreapp.features.user.profile.ui.components.AddAddressButton
import com.example.shoestoreapp.features.user.profile.ui.components.AddressItemCard
import com.example.shoestoreapp.features.user.profile.viewmodel.ManageAddressUiState
import com.example.shoestoreapp.features.user.profile.viewmodel.ManageAddressViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageAddressScreen(
    isSelectionMode: Boolean = false,
    viewModel: ManageAddressViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onAddAddressClick: () -> Unit = {},
    onEditAddressClick: (String) -> Unit = {},
    onAddressSelected: (String) -> Unit = {}
) {
    LaunchedEffect(Unit) {
        viewModel.fetchAddresses()
    }

    val uiState by viewModel.uiState.collectAsState()

    // Collect banner states from ViewModel
    val bannerMessage by viewModel.bannerMessage.collectAsState()
    val isBannerSuccess by viewModel.isBannerSuccess.collectAsState()
    val showBanner by viewModel.showBanner.collectAsState()

    // Wrap the Scaffold in a Box to overlay the TopBanner
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        UserTopBarTitle(text = "ADDRESSES")
                    },
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
                    .padding(paddingValues)
            ) {
                // Header Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "ADDRESSES",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1).sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Manage your delivery locations",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4C4546)
                    )
                }

                when (val state = uiState) {
                    is ManageAddressUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    is ManageAddressUiState.Error -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = state.message, color = Color(0xFF4C4546))
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.fetchAddresses() }) {
                                Text(text = "Retry")
                            }
                        }
                    }
                    is ManageAddressUiState.Empty -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "You haven't added any delivery addresses yet.",
                                color = Color(0xFF4C4546)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            AddAddressButton(
                                onClick = onAddAddressClick
                            )
                        }
                    }
                    is ManageAddressUiState.Success -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            items(state.addresses) { address ->
                                Box(
                                    modifier = Modifier.clickable(
                                        enabled = isSelectionMode,
                                        onClick = { onAddressSelected(address.id) }
                                    )
                                ) {
                                    AddressItemCard(
                                        address = address,
                                        onEditClick = { onEditAddressClick(address.id) },
                                        onRemoveClick = { viewModel.removeAddress(address.id) },
                                        onSetDefaultClick = { viewModel.setAsDefault(address.id) }
                                    )
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                                AddAddressButton(
                                    onClick = onAddAddressClick
                                )
                                Spacer(modifier = Modifier.height(32.dp))
                            }
                        }
                    }
                }
            }
        }

        // Overlay the TopBanner at the top center
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
