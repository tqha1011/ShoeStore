package com.example.shoestoreapp.features.auth.presentation.common // Change package if needed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestoreapp.core.utils.JwtUtils
import com.example.shoestoreapp.core.utils.TokenManager
import com.example.shoestoreapp.features.auth.domain.repository.AuthRepository
import kotlinx.coroutines.launch

abstract class BaseAuthViewModel(
    protected val repository: AuthRepository,
    protected val tokenManager: TokenManager
) : ViewModel() {

    // Force child classes to define their own UI update logic
    protected abstract fun updateLoadingState(isLoading: Boolean)
    protected abstract suspend fun onSocialLoginSuccess(role: String)
    protected abstract suspend fun onSocialLoginFailure(errorMessage: String)

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            updateLoadingState(true)
            val result = repository.loginWithGoogle(idToken)
            updateLoadingState(false)

            result.fold(
                onSuccess = { response ->
                    val token = response.token
                    val role = JwtUtils.getRoleFromToken(token)
                    tokenManager.saveAuthInfo(token = token, role = role)
                    onSocialLoginSuccess(role)
                },
                onFailure = { error ->
                    onSocialLoginFailure(error.message ?: "Google Login failed")
                }
            )
        }
    }

    fun loginWithFacebook(accessToken: String) {
        viewModelScope.launch {
            updateLoadingState(true)
            val result = repository.loginWithFacebook(accessToken)
            updateLoadingState(false)

            result.fold(
                onSuccess = { response ->
                    val token = response.token
                    val role = JwtUtils.getRoleFromToken(token)
                    tokenManager.saveAuthInfo(token = token, role = role)
                    onSocialLoginSuccess(role)
                },
                onFailure = { error ->
                    onSocialLoginFailure(error.message ?: "Facebook Login failed")
                }
            )
        }
    }
}