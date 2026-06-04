package com.example.shoestoreapp.features.user.profile.data.repositories

import android.util.Log
import com.example.shoestoreapp.features.user.profile.data.remote.AdministrativeApi
import com.example.shoestoreapp.features.user.profile.data.remote.AdministrativeApiService
import com.example.shoestoreapp.features.user.profile.data.remote.DistrictDto
import com.example.shoestoreapp.features.user.profile.data.remote.ProvinceDto
import com.example.shoestoreapp.features.user.profile.data.remote.WardDto

class AdministrativeRepositoryImpl(
    private val api: AdministrativeApi = AdministrativeApiService.api
) : AdministrativeRepository {
    override suspend fun getProvinces(): Result<List<ProvinceDto>> {
        return try {
            val response = api.getProvinces()
            if (response.isSuccessful) {
                Result.success(response.body().orEmpty())
            } else {
                Result.failure(Exception("Fetch provinces failed (HTTP ${response.code()})"))
            }
        } catch (e: Exception) {
            Log.e("API_ERROR", "Lỗi kéo API: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getDistricts(provinceCode: Int): Result<List<DistrictDto>> {
        return try {
            val response = api.getProvinceDetail(provinceCode)
            if (response.isSuccessful) {
                Result.success(response.body()?.districts.orEmpty())
            } else {
                Result.failure(Exception("Fetch districts failed (HTTP ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getWards(districtCode: Int): Result<List<WardDto>> {
        return try {
            val response = api.getDistrictDetail(districtCode)
            if (response.isSuccessful) {
                Result.success(response.body()?.wards.orEmpty())
            } else {
                Result.failure(Exception("Fetch wards failed (HTTP ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

