package com.example.shoestoreapp.features.admin.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestoreapp.features.user.profile.data.models.UserProfile
import com.example.shoestoreapp.features.user.profile.data.repositories.ProfileRepository
import com.example.shoestoreapp.features.user.profile.data.repositories.ProfileRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminProfileViewModel(
    private val repository: ProfileRepository = ProfileRepositoryImpl()
) : ViewModel() {
    private val _profileState = MutableStateFlow<UserProfile?>(null)
    val profileState: StateFlow<UserProfile?> = _profileState.asStateFlow()

    fun fetchAdminProfile() {
        viewModelScope.launch {
            repository.getUserProfile()
                .onSuccess { _profileState.value = it }
        }
    }
}

