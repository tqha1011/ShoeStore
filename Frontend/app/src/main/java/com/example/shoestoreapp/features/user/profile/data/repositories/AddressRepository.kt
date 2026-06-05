package com.example.shoestoreapp.features.user.profile.data.repositories

import com.example.shoestoreapp.features.user.profile.data.remote.AddressResponseDto
import com.example.shoestoreapp.features.user.profile.data.remote.CreateAddressDto

interface AddressRepository {
    suspend fun getAllAddresses(): Result<List<AddressResponseDto>>
    suspend fun createAddress(dto: CreateAddressDto): Result<Unit>
    suspend fun updateAddress(id: String, dto: CreateAddressDto): Result<Unit>
    suspend fun deleteAddress(id: String): Result<Unit>
    suspend fun getAddressById(id: String): Result<AddressResponseDto>
}

sealed class AddressRepositoryException(message: String) : Exception(message) {
    class BadRequest(message: String = ERROR_BAD_REQUEST) : AddressRepositoryException(message)
    class Unauthorized(message: String = ERROR_UNAUTHORIZED) : AddressRepositoryException(message)
    class NotFound(message: String = ERROR_NOT_FOUND) : AddressRepositoryException(message)
    class ServerError(message: String = ERROR_SERVER) : AddressRepositoryException(message)
    class Unknown(message: String = ERROR_UNKNOWN) : AddressRepositoryException(message)

    companion object {
        const val ERROR_BAD_REQUEST = "Invalid address data. Please check your information."
        const val ERROR_UNAUTHORIZED = "Unauthorized. Please sign in again."
        const val ERROR_NOT_FOUND = "Address not found."
        const val ERROR_SERVER = "Server error. Please try again later."
        const val ERROR_UNKNOWN = "Something went wrong with the address service."
    }
}