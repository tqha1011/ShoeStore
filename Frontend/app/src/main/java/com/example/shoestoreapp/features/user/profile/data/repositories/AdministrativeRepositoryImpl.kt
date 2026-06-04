package com.example.shoestoreapp.features.user.profile.data.repositories

import com.example.shoestoreapp.features.user.profile.data.remote.AdministrativeApi
import com.example.shoestoreapp.features.user.profile.data.remote.AdministrativeApiService
import com.example.shoestoreapp.features.user.profile.data.remote.DistrictDto
import com.example.shoestoreapp.features.user.profile.data.remote.ProvinceDto
import com.example.shoestoreapp.features.user.profile.data.remote.WardDto
import org.json.JSONObject
import retrofit2.Response

class AdministrativeRepositoryImpl(
    private val api: AdministrativeApi = AdministrativeApiService.api
) : AdministrativeRepository {

    override suspend fun getProvinces(): Result<List<ProvinceDto>> {
        return try {
            val response = api.getProvinces()
            if (response.isSuccessful) {
                Result.success(response.body().orEmpty())
            } else {
                Result.failure(response.toRepositoryException())
            }
        } catch (e: Exception) {
            Result.failure(AdministrativeRepositoryException.Unknown(e.message ?: AdministrativeRepositoryException.ERROR_UNKNOWN))
        }
    }

    override suspend fun getDistricts(provinceCode: Int): Result<List<DistrictDto>> {
        return try {
            val response = api.getProvinceDetail(provinceCode)
            if (response.isSuccessful) {
                Result.success(response.body()?.districts.orEmpty())
            } else {
                Result.failure(response.toRepositoryException())
            }
        } catch (e: Exception) {
            Result.failure(AdministrativeRepositoryException.Unknown(e.message ?: AdministrativeRepositoryException.ERROR_UNKNOWN))
        }
    }

    override suspend fun getWards(districtCode: Int): Result<List<WardDto>> {
        return try {
            val response = api.getDistrictDetail(districtCode)
            if (response.isSuccessful) {
                Result.success(response.body()?.wards.orEmpty())
            } else {
                Result.failure(response.toRepositoryException())
            }
        } catch (e: Exception) {
            Result.failure(AdministrativeRepositoryException.Unknown(e.message ?: AdministrativeRepositoryException.ERROR_UNKNOWN))
        }
    }

    // ================
    // LOGIC PARSE LỖI
    // ================

    private fun <T> Response<T>.toRepositoryException(): AdministrativeRepositoryException {
        val rawMessage = errorBody()?.string()?.takeIf { it.isNotBlank() }
        val backendMessage = parseBackendError(rawMessage)

        return when (code()) {
            400 -> AdministrativeRepositoryException.BadRequest(backendMessage ?: AdministrativeRepositoryException.ERROR_BAD_REQUEST)
            401 -> AdministrativeRepositoryException.Unauthorized(backendMessage ?: AdministrativeRepositoryException.ERROR_UNAUTHORIZED)
            404 -> AdministrativeRepositoryException.NotFound(backendMessage ?: AdministrativeRepositoryException.ERROR_NOT_FOUND)
            500 -> AdministrativeRepositoryException.ServerError(backendMessage ?: AdministrativeRepositoryException.ERROR_SERVER)
            else -> AdministrativeRepositoryException.Unknown(
                backendMessage ?: "${AdministrativeRepositoryException.ERROR_UNKNOWN} (HTTP ${code()})"
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