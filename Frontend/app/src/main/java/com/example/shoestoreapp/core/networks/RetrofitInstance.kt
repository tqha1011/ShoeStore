package com.example.shoestoreapp.core.networks

import android.content.Context
import com.example.shoestoreapp.core.utils.Constants
import com.example.shoestoreapp.features.admin.product.data.remote.AdminProductApi
import com.example.shoestoreapp.features.auth.data.remote.AuthApi
import com.example.shoestoreapp.features.user.product.data.remote.ProductApi
import com.example.shoestoreapp.features.user.cart.data.remote.CartApi
import com.example.shoestoreapp.features.admin.addproduct.data.remote.MasterDataApi
import com.example.shoestoreapp.features.user.checkout.data.remote.CheckOutApi
import com.example.shoestoreapp.features.admin.product.data.remote.ImageApi
import com.example.shoestoreapp.features.admin.voucher.data.remote.AdminVoucherApi
import com.example.shoestoreapp.features.user.profile.data.remote.ProfileApi
import com.example.shoestoreapp.features.user.voucher.data.remote.VoucherApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private val authInterceptor by lazy {
        AuthInterceptor(appContext)
    }

    // 1. Logging interceptor for debugging network requests
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

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

    // 4. Create ProductApi service for product endpoints
    val productApi: ProductApi by lazy {
        retrofit.create(ProductApi::class.java)
    }

    // 5. Create CartApi service for cart endpoints
    val cartApi: com.example.shoestoreapp.features.user.cart.data.remote.CartApi by lazy {
        retrofit.create(CartApi::class.java)
    }

    // 6. Create AdminApi service for admin endpoints
    val adminApi: AdminProductApi by lazy {
        retrofit.create(AdminProductApi::class.java)
    }

    // 7. Create ImageApi service for image upload endpoints
    val imageApi: ImageApi by lazy {
        retrofit.create(ImageApi::class.java)
    }

    // 8. Create MasterDataApi service for fetching master data (size, color, category)
    val masterDataApi: MasterDataApi by lazy {
        retrofit.create(MasterDataApi::class.java)
    }

    // 9. Create CheckOutApi service for checkout endpoints
    val checkOutApi: CheckOutApi by lazy {
        retrofit.create(CheckOutApi::class.java)
    }

    // 10. Create AdminVoucherApi service for admin voucher endpoints
    val adminVoucherApi: AdminVoucherApi by lazy {
        retrofit.create(AdminVoucherApi::class.java)
    }

    // 11. Create VoucherApi service for valid voucher endpoints
    val voucherApi: VoucherApi by lazy {
        retrofit.create(VoucherApi::class.java)
    }

    // 12. Create ProfileApi service for user profile endpoints
    val profileApi: ProfileApi by lazy {
        retrofit.create(ProfileApi::class.java)
    }
}