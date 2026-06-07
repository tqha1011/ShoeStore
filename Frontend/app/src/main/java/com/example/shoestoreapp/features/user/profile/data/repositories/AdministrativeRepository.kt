package com.example.shoestoreapp.features.user.profile.data.repositories

import com.example.shoestoreapp.features.user.profile.data.remote.DistrictDto
import com.example.shoestoreapp.features.user.profile.data.remote.ProvinceDto
import com.example.shoestoreapp.features.user.profile.data.remote.WardDto

interface AdministrativeRepository {
    suspend fun getProvinces(): Result<List<ProvinceDto>>
    suspend fun getDistricts(provinceCode: Int): Result<List<DistrictDto>>
    suspend fun getWards(districtCode: Int): Result<List<WardDto>>
}

sealed class AdministrativeRepositoryException(message: String) : Exception(message) {
    class BadRequest(message: String = ERROR_BAD_REQUEST) : AdministrativeRepositoryException(message)
    class Unauthorized(message: String = ERROR_UNAUTHORIZED) : AdministrativeRepositoryException(message)
    class NotFound(message: String = ERROR_NOT_FOUND) : AdministrativeRepositoryException(message)
    class ServerError(message: String = ERROR_SERVER) : AdministrativeRepositoryException(message)
    class Unknown(message: String = ERROR_UNKNOWN) : AdministrativeRepositoryException(message)

    companion object {
        const val ERROR_BAD_REQUEST = "Invalid administrative data request."
        const val ERROR_UNAUTHORIZED = "Unauthorized access."
        const val ERROR_NOT_FOUND = "Location data not found."
        const val ERROR_SERVER = "Server error. Please try again later."
        const val ERROR_UNKNOWN = "Something went wrong while fetching location data."
    }
}