package com.example.shoestoreapp.features.user.profile.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object AdministrativeApiService {
    private const val BASE_URL = "https://provinces.open-api.vn/"

    private val client by lazy {
        OkHttpClient.Builder().build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    val api: AdministrativeApi by lazy {
        retrofit.create(AdministrativeApi::class.java)
    }
}

