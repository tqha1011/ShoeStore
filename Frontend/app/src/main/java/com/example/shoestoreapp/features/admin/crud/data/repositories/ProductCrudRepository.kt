package com.example.shoestoreapp.features.admin.crud.data.repositories

import android.util.Log
import com.example.shoestoreapp.core.networks.RetrofitInstance
import com.example.shoestoreapp.features.admin.crud.data.remote.AdminProductCrudApi
import com.example.shoestoreapp.features.admin.crud.data.remote.ProductUpdateDtoRequest
import com.example.shoestoreapp.core.utils.Resource
import com.example.shoestoreapp.features.admin.crud.data.remote.ProductCreateDtoRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class ProductCrudRepository(
    private val productCrudApi: AdminProductCrudApi = RetrofitInstance.adminCrudApi
) {
    // 1. Create product
    fun adminCreateProduct(
        request: ProductCreateDtoRequest
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        val response = productCrudApi.adminCreateProduct(request)

        if (response.isSuccessful) {
            emit(Resource.Success(Unit))
        } else {
            // Lấy lỗi từ errorBody của response
            val errorMsg = response.errorBody()?.string() ?: "Tạo sản phẩm thất bại"
            emit(Resource.Error(errorMsg))
        }
    }.catch { e ->
        Log.e("ProductRepo", "Create failed: ${e.message}")
        emit(Resource.Error("Lỗi kết nối: ${e.localizedMessage}"))
    }.flowOn(Dispatchers.IO)

    // 2. Update product
    fun adminUpdateProduct(
        productGuid: String,
        request: ProductUpdateDtoRequest
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        val response = productCrudApi.adminUpdateProduct(productGuid, request)

        if (response.isSuccessful) {
            emit(Resource.Success(Unit))
        } else {
            val errorMsg = response.errorBody()?.string() ?: "Cập nhật thất bại"
            emit(Resource.Error(errorMsg))
        }
    }.catch { e ->
        Log.e("ProductRepo", "Update failed: ${e.message}")
        emit(Resource.Error("Lỗi hệ thống: ${e.localizedMessage}"))
    }.flowOn(Dispatchers.IO)

    // 3. Delete product by productGuid
    fun adminDeleteProduct(productGuid: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        val response = productCrudApi.adminDeleteProduct(productGuid)

        if (response.isSuccessful) {
            emit(Resource.Success(Unit))
        } else {
            emit(Resource.Error("Xóa thất bại (Mã lỗi: ${response.code()})"))
        }
    }.catch { e ->
        emit(Resource.Error("Không thể xóa: ${e.localizedMessage}"))
    }.flowOn(Dispatchers.IO)
}
