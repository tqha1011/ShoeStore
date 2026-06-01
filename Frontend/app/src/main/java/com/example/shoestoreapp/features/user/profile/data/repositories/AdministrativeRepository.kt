package com.example.shoestoreapp.features.user.profile.data.repositories

import com.example.shoestoreapp.features.user.profile.data.remote.DistrictDto
import com.example.shoestoreapp.features.user.profile.data.remote.ProvinceDto
import com.example.shoestoreapp.features.user.profile.data.remote.WardDto

interface AdministrativeRepository {
    suspend fun getProvinces(): Result<List<ProvinceDto>>
    suspend fun getDistricts(provinceCode: Int): Result<List<DistrictDto>>
    suspend fun getWards(districtCode: Int): Result<List<WardDto>>
}

