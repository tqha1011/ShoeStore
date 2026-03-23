package com.example.shoestoreapp.features.auth.presentation.sign_in

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shoestoreapp.core.utils.TokenManager
import com.example.shoestoreapp.features.auth.presentation.components.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.example.shoestoreapp.features.auth.presentation.common.AuthUiEvent

@Composable
fun LoginScreenContent(
    onNavigateToSignUp: () -> Unit = {},
    onNavigateToForgotPassword: () -> Unit = {},
    onNavigateToUserHome: () -> Unit = {},
    onNavigateToAdminHome: () -> Unit = {}
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val viewModel: SignInViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SignInViewModel(tokenManager = tokenManager) as T
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
                is AuthUiEvent.NavigateToUserHome -> onNavigateToUserHome()
                is AuthUiEvent.NavigateToAdminHome -> onNavigateToAdminHome()
                is AuthUiEvent.NavigateToSignUp -> onNavigateToSignUp()
                else -> Unit
            }
        }
    }

    val signInTheme = AuthThemeConfig(
        backgroundColor = Color.White,
        canvasColor = Color.Black,
        topBarContentColor = Color.Black,
        topBarButtonContainerColor = Color.White
    )

    AuthScreenTemplate(
        title = "Sign In",
        topBarButtonText = "Sign up",
        onTopBarButtonClick = onNavigateToSignUp,
        theme = signInTheme
    ) {
        Spacer(modifier = Modifier.weight(1.8f))
        TitleText("Sign In", color = Color.Black)
        Spacer(modifier = Modifier.weight(2f))

        val inputStyle = AuthFieldStyle(label = "Email", containerColor = Color(0xFF222222), textColor = Color.White)

        AuthTextField(
            value = state.email,
            onValueChange = { viewModel.onEvent(SignInEvent.EmailChanged(it)) },
            style = inputStyle,
            isError = state.emailError != null,
            errorText = state.emailError
        )

        Spacer(modifier = Modifier.height(20.dp))

        AuthPasswordField(
            value = state.password,
            onValueChange = { viewModel.onEvent(SignInEvent.PasswordChanged(it)) },
            style = inputStyle.copy(label = "Password"),
            isError = state.passwordError != null,
            errorText = state.passwordError,
            passwordVisible = state.isPasswordVisible,
            onToggleVisibility = { viewModel.onEvent(SignInEvent.TogglePasswordVisibility) }
        )

        Spacer(modifier = Modifier.height(32.dp))

        AuthActionButton(
            text = if (state.isLoading) "Loading..." else "Sign in",
            icon = Icons.Default.Key,
            onClick = { if (!state.isLoading) viewModel.onEvent(SignInEvent.Submit) },
            containerColor = Color(0xFF1A1A1A),
            contentColor = Color.White
        )

        Text(
            text = "Forgot password?",
            fontSize = 16.sp,
            color = Color.LightGray,
            modifier = Modifier.align(Alignment.End).padding(top = 8.dp).clickable { onNavigateToForgotPassword() }
        )

        Spacer(modifier = Modifier.weight(1f))

        SocialLoginSection(
            dividerColor = Color.DarkGray,
            textColor = Color.DarkGray,
            buttonContainerColor = Color(0xFF222222),
            iconTint = Color.White,
            onGoogleClick = { coroutineScope.launch { googleAuthClient.signIn()?.let { viewModel.loginWithGoogle(it) } } },
            onFacebookClick = { triggerFacebookLogin() }
        )
        Spacer(modifier = Modifier.height(30.dp))
    }
}
