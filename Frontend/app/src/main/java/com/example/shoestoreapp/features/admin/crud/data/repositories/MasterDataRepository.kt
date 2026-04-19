package com.example.shoestoreapp.features.admin.crud.data.repositories

import com.example.shoestoreapp.core.networks.RetrofitInstance
import com.example.shoestoreapp.core.utils.Resource
import com.example.shoestoreapp.features.admin.crud.data.remote.master_data.CategoryDto
import com.example.shoestoreapp.features.admin.crud.data.remote.master_data.ColorDto
import com.example.shoestoreapp.features.admin.crud.data.remote.master_data.SizeDto
import com.example.shoestoreapp.features.admin.crud.data.remote.MasterDataApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class MasterDataRepository(
    private val api: MasterDataApi = RetrofitInstance.masterDataApi
) {

    // 1. Lấy danh sách Size
    fun getSizes(): Flow<Resource<List<SizeDto>>> = flow<Resource<List<SizeDto>>> {
        emit(Resource.Loading)
        val response = api.getSizes()
        if (response.isSuccessful) {
            // Trả về list dữ liệu hoặc list rỗng nếu body null
            emit(Resource.Success(response.body() ?: emptyList()))
        } else {
            val error = response.errorBody()?.string() ?: "Không thể lấy danh sách Size"
            emit(Resource.Error(error))
        }
    }.catch { e ->
        emit(Resource.Error("Lỗi kết nối Size: ${e.localizedMessage}"))
    }.flowOn(Dispatchers.IO)

    // 2. Lấy danh sách Color
    fun getColors(): Flow<Resource<List<ColorDto>>> = flow<Resource<List<ColorDto>>> {
        emit(Resource.Loading)
        val response = api.getColors()
        if (response.isSuccessful) {
            emit(Resource.Success(response.body() ?: emptyList()))
        } else {
            val error = response.errorBody()?.string() ?: "Không thể lấy danh sách Màu"
            emit(Resource.Error(error))
        }
    }.catch { e ->
        emit(Resource.Error("Lỗi kết nối Màu: ${e.localizedMessage}"))
    }.flowOn(Dispatchers.IO)

    // 3. Lấy danh sách Category
    fun getCategories(): Flow<Resource<List<CategoryDto>>> = flow<Resource<List<CategoryDto>>> {
        emit(Resource.Loading)
        val response = api.getCategories()
        if (response.isSuccessful) {
            emit(Resource.Success(response.body() ?: emptyList()))
        } else {
            val error = response.errorBody()?.string() ?: "Không thể lấy danh sách Loại"
            emit(Resource.Error(error))
        }
    }.catch { e ->
        emit(Resource.Error("Lỗi kết nối Loại: ${e.localizedMessage}"))
    }.flowOn(Dispatchers.IO)
}