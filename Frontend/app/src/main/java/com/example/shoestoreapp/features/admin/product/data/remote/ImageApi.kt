package com.example.shoestoreapp.features.admin.product.data.remote

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

fun interface ImageApi {
    @Multipart
    @POST("/api/Image/image")
    suspend fun uploadImage(@Part file: MultipartBody.Part): Response<ImageUploadResponseDto>
}
