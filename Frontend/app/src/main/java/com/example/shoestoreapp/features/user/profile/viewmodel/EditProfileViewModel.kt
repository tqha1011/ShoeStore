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
    val errorMessage: String? = null,
    val bannerMessage: String = "",
    val isBannerSuccess: Boolean = true,
    val showBanner: Boolean = false
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

    fun hideBanner() {
        _uiState.update { it.copy(showBanner = false) }
    }

    fun onSaveClicked(context: Context) {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val avatarUrl = _uiState.value.avatarUri?.let { uri ->
                val file = context.uriToTempFile(uri)
                imageRepository.uploadImage(file)
                    .getOrElse {
                        val errorMsg = it.message ?: "Unable to upload image."
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                bannerMessage = errorMsg,
                                isBannerSuccess = false,
                                showBanner = true
                            )
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
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = true,
                            bannerMessage = "Profile updated successfully",
                            isBannerSuccess = true,
                            showBanner = true
                        )
                    }
                }
                .onFailure { throwable ->
                    val realErrorMessage = if (throwable is HttpException) {
                        parseValidationError(throwable)
                    } else {
                        throwable.message ?: "Update failed."
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            bannerMessage = realErrorMessage,
                            isBannerSuccess = false,
                            showBanner = true
                        )
                    }
                }
        }
    }

    private fun fetchProfile() {
        viewModelScope.launch {
            repository.getUserProfile()
                .onSuccess { initData(it) }
                .onFailure { throwable ->
                    val errorMsg = throwable.message ?: "Unable to load profile."
                    _uiState.update { state ->
                        state.copy(
                            bannerMessage = errorMsg,
                            isBannerSuccess = false,
                            showBanner = true
                        )
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

    // Hàm phụ trợ lấy lỗi chi tiết
    private fun parseValidationError(exception: HttpException): String {
        return try {
            val errorString = exception.response()?.errorBody()?.string()
            if (!errorString.isNullOrEmpty()) {
                val jsonObject = JSONObject(errorString)

                // Ưu tiên 1: Vào mảng errors để lấy lỗi chi tiết
                if (jsonObject.has("errors")) {
                    val errorsObj = jsonObject.getJSONObject("errors")
                    val keys = errorsObj.keys()
                    if (keys.hasNext()) {
                        val firstKey = keys.next()
                        val errorArray = errorsObj.getJSONArray(firstKey)
                        if (errorArray.length() > 0) {
                            return errorArray.getString(0)
                        }
                    }
                }

                // Ưu tiên 2: Nếu không có mảng errors thì lấy title
                if (jsonObject.has("title")) {
                    return jsonObject.getString("title")
                }

                // Ưu tiên 3: Lấy message thông thường
                if (jsonObject.has("message")) {
                    return jsonObject.getString("message")
                }
            }
            "Update failed (HTTP ${exception.code()})"
        } catch (e: Exception) {
            "Update failed (HTTP ${exception.code()})"
        }
    }
}