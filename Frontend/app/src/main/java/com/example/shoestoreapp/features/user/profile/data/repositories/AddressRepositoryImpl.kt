package com.example.shoestoreapp.features.user.profile.data.repositories

import com.example.shoestoreapp.core.networks.RetrofitInstance
import com.example.shoestoreapp.features.user.profile.data.remote.AddressApi
import com.example.shoestoreapp.features.user.profile.data.remote.AddressResponseDto
import com.example.shoestoreapp.features.user.profile.data.remote.CreateAddressDto

class AddressRepositoryImpl(
    private val api: AddressApi = RetrofitInstance.addressApi
) : AddressRepository {
    override suspend fun getAllAddresses(): Result<List<AddressResponseDto>> {
        return try {
            val response = api.getAllAddresses()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Fetch addresses failed (HTTP ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createAddress(dto: CreateAddressDto): Result<Unit> {
        return try {
            val response = api.createAddress(dto)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Create address failed (HTTP ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
