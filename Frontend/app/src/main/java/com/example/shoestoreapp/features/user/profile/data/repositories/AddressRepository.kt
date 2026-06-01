package com.example.shoestoreapp.features.user.profile.data.repositories

import com.example.shoestoreapp.features.user.profile.data.remote.AddressResponseDto
import com.example.shoestoreapp.features.user.profile.data.remote.CreateAddressDto

interface AddressRepository {
    suspend fun getAllAddresses(): Result<List<AddressResponseDto>>
    suspend fun createAddress(dto: CreateAddressDto): Result<Unit>
}
