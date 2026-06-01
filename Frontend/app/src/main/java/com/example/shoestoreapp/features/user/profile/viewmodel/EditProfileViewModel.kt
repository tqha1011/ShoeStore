package com.example.shoestoreapp.features.user.profile.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestoreapp.core.utils.uriToTempFile
import com.example.shoestoreapp.features.admin.product.data.repositories.ImageRepository
import com.example.shoestoreapp.features.admin.product.data.repositories.ImageRepositoryImpl
import com.example.shoestoreapp.features.user.profile.data.models.UserProfile
import com.example.shoestoreapp.features.user.profile.data.remote.UpdateProfileDto
import com.example.shoestoreapp.features.user.profile.data.repositories.ProfileRepository
import com.example.shoestoreapp.features.user.profile.data.repositories.ProfileRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import retrofit2.HttpException
import org.json.JSONObject

data class EditProfileUiState(
    val userName: String = "",
    val avatarUri: Uri? = null,
    val currentAvatarUrl: String? = null,
    val dateOfBirthUi: String = "",
    val dateOfBirthIso: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

class EditProfileViewModel(
    private val repository: ProfileRepository = ProfileRepositoryImpl(),
    private val imageRepository: ImageRepository = ImageRepositoryImpl()
) : ViewModel() {
    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val displayFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.US)
    private val storedFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val profileFormatter = SimpleDateFormat("dd MMM yyyy", Locale.US)

    init {
        fetchProfile()
    }

    fun initData(profile: UserProfile) {
        val parsedDate = parseProfileDate(profile.birthday)
        _email.value = profile.email
        _uiState.update {
            it.copy(
                userName = profile.name,
                currentAvatarUrl = profile.avatarUrl.ifBlank { null },
                dateOfBirthUi = parsedDate.first,
                dateOfBirthIso = parsedDate.second
            )
        }
    }

    fun onUserNameChanged(value: String) {
        _uiState.update { it.copy(userName = value) }
    }

    fun onAvatarSelected(uri: Uri) {
        _uiState.update { it.copy(avatarUri = uri) }
    }

    fun onDateSelected(millis: Long) {
        val date = Date(millis)
        _uiState.update {
            it.copy(
                dateOfBirthUi = displayFormatter.format(date),
                dateOfBirthIso = storedFormatter.format(date)
            )
        }
    }

    fun onSaveClicked(context: Context) {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val avatarUrl = _uiState.value.avatarUri?.let { uri ->
                val file = context.uriToTempFile(uri)
                imageRepository.uploadImage(file)
                    .getOrElse {
                        _uiState.update { state ->
                            state.copy(isLoading = false, errorMessage = it.message ?: "Unable to upload image.")
                        }
                        return@launch
                    }
                    .imageUrl
            } ?: _uiState.value.currentAvatarUrl

            val dto = UpdateProfileDto(
                userName = _uiState.value.userName,
                avatarUrl = avatarUrl,
                dateOfBirth = _uiState.value.dateOfBirthIso.ifBlank { null }
            )

            repository.updateProfile(dto)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                .onFailure { throwable ->
                    var realErrorMessage = "Update failed."
                    if (throwable is HttpException) {
                        try {
                            val errorBodyString = throwable.response()?.errorBody()?.string()
                            if (!errorBodyString.isNullOrEmpty()) {
                                val jsonObject = JSONObject(errorBodyString)
                                if (jsonObject.has("title")) {
                                    realErrorMessage = jsonObject.getString("title")
                                } else if (jsonObject.has("message")) {
                                    realErrorMessage = jsonObject.getString("message")
                                } else {
                                    realErrorMessage = errorBodyString
                                }
                            }
                        } catch (e: Exception) {
                            realErrorMessage = "Server error: HTTP ${throwable.code()}"
                        }
                    } else {
                        realErrorMessage = throwable.message ?: "Update failed."
                    }

                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = realErrorMessage)
                    }
                }
        }
    }

    private fun fetchProfile() {
        viewModelScope.launch {
            repository.getUserProfile()
                .onSuccess { initData(it) }
                .onFailure { throwable ->
                    _uiState.update { state ->
                        state.copy(errorMessage = throwable.message ?: "Unable to load profile.")
                    }
                }
        }
    }

    private fun parseProfileDate(raw: String): Pair<String, String> {
        if (raw.isBlank()) return "" to ""
        return try {
            val date = profileFormatter.parse(raw)
            if (date != null) {
                displayFormatter.format(date) to storedFormatter.format(date)
            } else {
                raw to ""
            }
        } catch (e: Exception) {
            raw to ""
        }
    }

    // =========================================
    // CÁC HÀM XÓA TRẠNG THÁI (CLEAR STATE) DÙNG CHO UI
    // =========================================

    /**
     * Gọi hàm này bên UI sau khi Toast lỗi đã được hiển thị để tránh lặp Toast
     */
    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Gọi hàm này bên UI sau khi xử lý xong sự kiện thành công
     */
    fun clearSuccessState() {
        _uiState.update { it.copy(isSuccess = false) }
    }
}