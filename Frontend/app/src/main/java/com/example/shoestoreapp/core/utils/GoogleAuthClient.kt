package com.example.shoestoreapp.core.utils

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.example.shoestoreapp.BuildConfig

class GoogleAuthClient(
    private val context: Context,
    private val credentialManager: CredentialManager = CredentialManager.create(context)
) {

    suspend fun signIn(): String? {
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false) // Show all Google accounts on device
                .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID)
                .setAutoSelectEnabled(false) // Disable auto select to let user choose account
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            Log.d("GoogleAuth", "Calling Credential Manager with Client ID: ${BuildConfig.GOOGLE_CLIENT_ID}")

            val result = credentialManager.getCredential(context, request)
            val credential = result.credential

            Log.d("GoogleAuth", "Received credential type: ${credential.type}")

            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential =
                    GoogleIdTokenCredential.createFrom(credential.data)

                Log.d("GoogleAuth", "Successfully retrieved ID Token!")
                googleIdTokenCredential.idToken
            } else {
                Log.e("GoogleAuth", "Invalid credential type for Google ID Token")
                null
            }

        } catch (e: GetCredentialException) {
            Log.e(
                "GoogleAuth",
                "Credential Manager error: ${e.message} - Type: ${e.type}"
            )
            null

        } catch (e: Exception) {
            Log.e(
                "GoogleAuth",
                "Unexpected error during Google Sign-In: ${e.message}"
            )
            e.printStackTrace()
            null
        }
    }
}