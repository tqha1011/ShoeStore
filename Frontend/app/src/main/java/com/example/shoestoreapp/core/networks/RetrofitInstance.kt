package com.example.shoestoreapp.core.networks

import com.example.shoestoreapp.core.utils.Constants
import com.example.shoestoreapp.features.auth.data.remote.AuthApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    // 1. Logging interceptor for debugging network requests
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    // 2. Core Retrofit instance
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    // 3. Create AuthApi service for authentication endpoints
    val authApi: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }
}