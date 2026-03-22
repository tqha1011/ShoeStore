package com.example.shoestoreapp.core.networks

import com.example.shoestoreapp.core.utils.Constants
import com.example.shoestoreapp.features.auth.data.remote.AuthApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    // 1. Vẫn giữ cái Camera giám sát để bắt bug cho dễ
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    // 2. Cục Retrofit cốt lõi
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    // 3. ĐÂY CHÍNH LÀ ĐOẠN GIỐNG TRONG VIDEO MÀ M THẮC MẮC NÀY:
    // Khởi tạo AuthApi để chọc vào các API liên quan đến Auth
    val authApi: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }
}