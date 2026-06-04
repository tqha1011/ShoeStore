package com.example.shoestoreapp.features.user.profile.data.repositories

import com.example.shoestoreapp.core.networks.RetrofitInstance
import com.example.shoestoreapp.features.user.profile.data.remote.AddressApi
import com.example.shoestoreapp.features.user.profile.data.remote.AddressResponseDto
import com.example.shoestoreapp.features.user.profile.data.remote.CreateAddressDto
import org.json.JSONObject
import retrofit2.Response

class AddressRepositoryImpl(
    private val api: AddressApi = RetrofitInstance.addressApi
) : AddressRepository {

    override suspend fun getAllAddresses(): Result<List<AddressResponseDto>> {
        return try {
            val response = api.getAllAddresses()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(response.toRepositoryException())
            }
        } catch (e: Exception) {
            Result.failure(AddressRepositoryException.Unknown(e.message ?: AddressRepositoryException.ERROR_UNKNOWN))
        }
    }

    override suspend fun createAddress(dto: CreateAddressDto): Result<Unit> {
        return try {
            val response = api.createAddress(dto)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(response.toRepositoryException())
            }
        } catch (e: Exception) {
            Result.failure(AddressRepositoryException.Unknown(e.message ?: AddressRepositoryException.ERROR_UNKNOWN))
        }
    }

    override suspend fun updateAddress(id: String, dto: CreateAddressDto): Result<Unit> {
        return try {
            val response = api.updateAddress(id, dto)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(response.toRepositoryException())
            }
        } catch (e: Exception) {
            Result.failure(AddressRepositoryException.Unknown(e.message ?: AddressRepositoryException.ERROR_UNKNOWN))
        }
    }

    override suspend fun deleteAddress(id: String): Result<Unit> {
        return try {
            val response = api.deleteAddress(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(response.toRepositoryException())
            }
        } catch (e: Exception) {
            Result.failure(AddressRepositoryException.Unknown(e.message ?: AddressRepositoryException.ERROR_UNKNOWN))
        }
    }

    override suspend fun getAddressById(id: String): Result<AddressResponseDto> {
        return try {
            val response = api.getAddressById(id)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(AddressRepositoryException.NotFound(AddressRepositoryException.ERROR_NOT_FOUND))
            } else {
                Result.failure(response.toRepositoryException())
            }
        } catch (e: Exception) {
            Result.failure(AddressRepositoryException.Unknown(e.message ?: AddressRepositoryException.ERROR_UNKNOWN))
        }
    }

    // ================
    // LOGIC PARSE LỖI
    // ================

    private fun <T> Response<T>.toRepositoryException(): AddressRepositoryException {
        val rawMessage = errorBody()?.string()?.takeIf { it.isNotBlank() }
        val backendMessage = parseBackendError(rawMessage)

        return when (code()) {
            400 -> AddressRepositoryException.BadRequest(backendMessage ?: AddressRepositoryException.ERROR_BAD_REQUEST)
            401 -> AddressRepositoryException.Unauthorized(backendMessage ?: AddressRepositoryException.ERROR_UNAUTHORIZED)
            404 -> AddressRepositoryException.NotFound(backendMessage ?: AddressRepositoryException.ERROR_NOT_FOUND)
            500 -> AddressRepositoryException.ServerError(backendMessage ?: AddressRepositoryException.ERROR_SERVER)
            else -> AddressRepositoryException.Unknown(
                backendMessage ?: "${AddressRepositoryException.ERROR_UNKNOWN} (HTTP ${code()})"
            )
        }
    }

    private fun parseBackendError(rawMessage: String?): String? {
        if (rawMessage.isNullOrBlank()) return null
        return try {
            val jsonObject = JSONObject(rawMessage)

            if (jsonObject.has("errors")) {
                val errorsObj = jsonObject.getJSONObject("errors")
                val errorMessages = mutableListOf<String>()
                val keys = errorsObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val errorArray = errorsObj.getJSONArray(key)
                    for (i in 0 until errorArray.length()) {
                        errorMessages.add(errorArray.getString(i))
                    }
                }
                if (errorMessages.isNotEmpty()) return errorMessages.joinToString("\n")
            }
            if (jsonObject.has("title")) return jsonObject.getString("title")
            if (jsonObject.has("message")) return jsonObject.getString("message")

            rawMessage
        } catch (_: Exception) {
            rawMessage
        }
    }
}