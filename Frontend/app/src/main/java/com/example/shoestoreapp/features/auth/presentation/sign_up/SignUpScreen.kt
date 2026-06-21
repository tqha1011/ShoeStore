package com.example.shoestoreapp.features.auth.presentation.sign_up

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shoestoreapp.core.utils.TokenManager
import com.example.shoestoreapp.features.auth.presentation.components.AuthUiEvent
import com.example.shoestoreapp.features.auth.presentation.components.*
import com.example.shoestoreapp.features.user.product.ui.components.TopBanner
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun RegisterScreenContent(
    onNavigateToSignIn: () -> Unit = {},
    onNavigateToUserHome: () -> Unit = {},
    onNavigateToVerifyOtp: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val viewModel: SignUpViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SignUpViewModel(tokenManager = tokenManager) as T
            }
        }
    )

    val coroutineScope = rememberCoroutineScope()
    val googleAuthClient = remember { com.example.shoestoreapp.core.utils.GoogleAuthClient(context) }
    val triggerFacebookLogin = com.example.shoestoreapp.core.utils.rememberFacebookLogin(
        onAuthComplete = { it?.let { viewModel.loginWithFacebook(it) } }
    )
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is AuthUiEvent.NavigateToSignIn -> onNavigateToSignIn()
                is AuthUiEvent.NavigateToUserHome -> onNavigateToUserHome()
                is AuthUiEvent.NavigateToSignUpOtp -> onNavigateToVerifyOtp(event.email)
                else -> Unit
            }
        }
    }

    val signUpTheme = AuthThemeConfig(
        backgroundColor = Color.Black,
        canvasColor = Color.White,
        topBarContentColor = Color.White,
        topBarButtonContainerColor = Color.Black
    )

    Box(modifier = Modifier.fillMaxSize()) {
        AuthScreenTemplate(
            topBarButtonText = "Sign in",
            onTopBarButtonClick = onNavigateToSignIn,
            theme = signUpTheme
        ) {
            Spacer(modifier = Modifier.height(118.dp))
            TitleText("Sign Up", color = Color.White)
            Spacer(modifier = Modifier.height(94.dp))

            val inputStyle = AuthFieldStyle(label = "Email", containerColor = Color(0xFFF5F5F5), textColor = Color.Black, unfocusedBorderColor = Color.LightGray)

            AuthTextField(
                value = state.email,
                onValueChange = { viewModel.onEvent(SignUpEvent.EmailChanged(it)) },
                style = inputStyle,
                isError = state.emailError != null,
                errorText = state.emailError,
                showErrorAboveField = true
            )

            Spacer(modifier = Modifier.height(4.dp))

            AuthPasswordField(
                value = state.password,
                onValueChange = { viewModel.onEvent(SignUpEvent.PasswordChanged(it)) },
                style = inputStyle.copy(label = "Password"),
                isError = state.passwordError != null,
                errorText = state.passwordError,
                passwordVisible = state.isPasswordVisible,
                onToggleVisibility = { viewModel.onEvent(SignUpEvent.TogglePasswordVisibility) }
            )

            Spacer(modifier = Modifier.height(4.dp))

            AuthPasswordField(
                value = state.confirmPassword,
                onValueChange = { viewModel.onEvent(SignUpEvent.ConfirmPasswordChanged(it)) },
                style = inputStyle.copy(label = "Confirm Password"),
                isError = state.confirmPasswordError != null,
                errorText = state.confirmPasswordError,
                passwordVisible = state.isConfirmPasswordVisible,
                onToggleVisibility = { viewModel.onEvent(SignUpEvent.ToggleConfirmPasswordVisibility) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            AuthActionButton(
                text = if (state.isLoading) "Loading..." else "Sign up",
                icon = Icons.Default.Key,
                onClick = { if (!state.isLoading) viewModel.onEvent(SignUpEvent.Submit) },
                containerColor = Color.White,
                contentColor = Color.Black
            )

            Spacer(modifier = Modifier.weight(1f))

            SocialLoginSection(
                dividerColor = Color.Black,
                textColor = Color.Black,
                buttonContainerColor = Color(0xFFF5F5F5),
                iconTint = Color.Black,
                onGoogleClick = {
                    coroutineScope.launch {
                        val idToken = googleAuthClient.signIn()
                        if (idToken != null) {
                            viewModel.loginWithGoogle(idToken)
                        } else {
                            viewModel.onSocialLoginFailed("Google sign-in failed. Check Google client configuration.")
                        }
                    }
                },
                onFacebookClick = { triggerFacebookLogin() }
            )
            Spacer(modifier = Modifier.height(30.dp))
        }

        Box(modifier = Modifier.align(Alignment.TopCenter)) {
            TopBanner(
                message = state.bannerMessage,
                isSuccess = state.isBannerSuccess,
                isVisible = state.showBanner,
                onDismiss = { viewModel.hideBanner() }
            )
        }
    }
}
