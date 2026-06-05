package com.example.shoestoreapp.features.admin.crud.data.repositories

import android.util.Log
import com.example.shoestoreapp.core.networks.RetrofitInstance
import com.example.shoestoreapp.features.admin.crud.data.remote.AdminProductCrudApi
import com.example.shoestoreapp.features.admin.crud.data.remote.ProductUpdateDtoRequest
import com.example.shoestoreapp.core.utils.Resource
import com.example.shoestoreapp.features.admin.crud.data.remote.ProductCreateDtoRequest
import com.example.shoestoreapp.features.admin.crud.data.remote.ProductCreateVariantDto
import java.io.File
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
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

    // 4. Add variant to an existing product
    fun adminAddVariant(
        productGuid: String,
        request: ProductCreateVariantDto,
        imageFile: File? = null
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        val textMediaType = "text/plain".toMediaType()
        val fields = linkedMapOf(
            "sizeId" to request.sizeId.toString().toRequestBody(textMediaType),
            "size" to request.size.toString().toRequestBody(textMediaType),
            "colorId" to request.colorId.toString().toRequestBody(textMediaType),
            "colorName" to request.colorName.toRequestBody(textMediaType),
            "stock" to request.stock.toString().toRequestBody(textMediaType),
            "price" to request.price.toString().toRequestBody(textMediaType),
            "isSelling" to request.isSelling.toString().toRequestBody(textMediaType)
        )

        val imagePart = imageFile?.let { file ->
            val imageBody: RequestBody = file.readBytes().toRequestBody("image/*".toMediaType())
            MultipartBody.Part.createFormData("image", file.name, imageBody)
        }

        val response = productCrudApi.adminAddVariant(productGuid, fields, imagePart)

        if (response.isSuccessful) {
            emit(Resource.Success(Unit))
        } else {
            val errorMsg = response.errorBody()?.string() ?: "Thêm biến thể thất bại"
            emit(Resource.Error(errorMsg))
        }
    }.catch { e ->
        Log.e("ProductRepo", "Add variant failed: ${e.message}")
        emit(Resource.Error("Lỗi hệ thống: ${e.localizedMessage}"))
    }.flowOn(Dispatchers.IO)
}
