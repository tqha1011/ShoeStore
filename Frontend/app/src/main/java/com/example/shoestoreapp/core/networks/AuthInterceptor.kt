package com.example.shoestoreapp.core.networks
import android.content.Context
import com.example.shoestoreapp.core.utils.JwtUtils
import com.example.shoestoreapp.core.utils.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val tokenManager = TokenManager(context)

        // Lấy token đồng bộ từ DataStore
        val token = runBlocking {
            tokenManager.getToken.first()
        }

        val requestBuilder = chain.request().newBuilder()
        val normalizedToken = JwtUtils.normalizeToken(token)

        normalizedToken?.let {
            requestBuilder.header("Authorization", "Bearer $it")
        }

        return chain.proceed(requestBuilder.build())
    }
}