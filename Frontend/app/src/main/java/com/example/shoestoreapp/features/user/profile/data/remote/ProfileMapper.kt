package com.example.shoestoreapp.features.user.profile.data.remote

import com.example.shoestoreapp.features.user.profile.data.models.UserProfile
import java.text.SimpleDateFormat
import java.util.Locale

private val inputDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
private val outputDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)

fun ResponseProfileDto.toUserProfile(): UserProfile {
    val formattedDob = dateOfBirth?.let { raw ->
        try {
            val parsed = inputDateFormat.parse(raw)
            if (parsed != null) outputDateFormat.format(parsed) else ""
        } catch (e: Exception) {
            ""
        }
    } ?: ""

    return UserProfile(
        name = userName.orEmpty(),
        email = email.orEmpty(),
        birthday = formattedDob,
        avatarUrl = avatarUrl.orEmpty()
    )
}

