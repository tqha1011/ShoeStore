package com.example.shoestoreapp.core.utils

import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult

@Composable
fun rememberFacebookLogin(
    onAuthComplete: (String?) -> Unit
): () -> Unit {
    // 1. Initialize CallbackManager to receive results from Facebook
    val callbackManager = remember { CallbackManager.Factory.create() }
    val loginManager = LoginManager.getInstance()

    // Get Compose context so Facebook can launch the login UI
    val registryOwner = LocalActivityResultRegistryOwner.current

    // 2. Register callback to listen for results (Success, Cancel, Error)
    DisposableEffect(Unit) {
        loginManager.registerCallback(
            callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    // Success -> extract access token and return it to ViewModel
                    onAuthComplete(result.accessToken.token)
                }

                override fun onCancel() {
                    // User canceled login
                    onAuthComplete(null)
                }

                override fun onError(error: FacebookException) {
                    // Network or system error
                    onAuthComplete(null)
                }
            }
        )
        onDispose {
            loginManager.unregisterCallback(callbackManager)
        }
    }

    // 3. Return an action to attach to UI button
    return {
        registryOwner?.let {
            // Request permissions: email and public profile
            loginManager.logIn(it, callbackManager, listOf("email", "public_profile"))
        }
    }
}