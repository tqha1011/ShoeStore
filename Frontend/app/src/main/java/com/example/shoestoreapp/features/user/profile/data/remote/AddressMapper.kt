package com.example.shoestoreapp.features.user.profile.data.remote

import com.example.shoestoreapp.features.user.profile.data.models.AddressUiModel

fun AddressResponseDto.toAddressUiModel(): AddressUiModel {
    return AddressUiModel(
        id = id,
        fullAddress = address,
        isDefault = isDefault
    )
}

