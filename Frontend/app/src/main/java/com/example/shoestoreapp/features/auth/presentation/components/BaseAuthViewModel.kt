package com.example.shoestoreapp.features.auth.presentation.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestoreapp.core.utils.JwtUtils
import com.example.shoestoreapp.core.utils.TokenManager
import com.example.shoestoreapp.features.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class BaseAuthViewModel<S>(
    protected val repository: AuthRepository,
    protected val tokenManager: TokenManager,
    initialState: S
) : ViewModel() {

    // Move state declaration here to avoid duplication in child classes
    protected val _state = MutableStateFlow(initialState)
    val state = _state.asStateFlow()

    protected abstract fun updateLoading(isLoading: Boolean)
    protected abstract fun handleSocialSuccess(role: String)
    protected abstract fun handleSocialError(message: String)

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            updateLoading(true)
            repository.loginWithGoogle(idToken).fold(
                onSuccess = { response ->
                    val role = JwtUtils.getRoleFromToken(response.token)
                    tokenManager.saveAuthInfo(response.token, role)
                    handleSocialSuccess(role)
                },
                onFailure = { handleSocialError(it.message ?: "Google Login failed") }
            )
            updateLoading(false)
        }
    }

    fun loginWithFacebook(accessToken: String) {
        viewModelScope.launch {
            updateLoading(true)
            repository.loginWithFacebook(accessToken).fold(
                onSuccess = { response ->
                    val role = JwtUtils.getRoleFromToken(response.token)
                    tokenManager.saveAuthInfo(response.token, role)
                    handleSocialSuccess(role)
                },
                onFailure = { handleSocialError(it.message ?: "Facebook Login failed") }
            )
            updateLoading(false)
        }
    }
}