package com.example.shoestoreapp.features.auth.presentation.sign_up

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shoestoreapp.core.utils.TokenManager
import com.example.shoestoreapp.features.auth.presentation.common.AuthUiEvent
import com.example.shoestoreapp.features.auth.presentation.components.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun RegisterScreenContent(
    onNavigateToSignIn: () -> Unit = {},
    onNavigateToUserHome: () -> Unit = {},
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

    AuthScreenTemplate(
        topBarButtonText = "Sign in",
        onTopBarButtonClick = onNavigateToSignIn,
        theme = signUpTheme
    ) {
        Spacer(modifier = Modifier.weight(4f))
        TitleText("Sign Up", color = Color.White)
        Spacer(modifier = Modifier.weight(4f))

        val inputStyle = AuthFieldStyle(label = "Email", containerColor = Color(0xFFF5F5F5), textColor = Color.Black, unfocusedBorderColor = Color.LightGray)

        AuthTextField(
            value = state.email,
            onValueChange = { viewModel.onEvent(SignUpEvent.EmailChanged(it)) },
            style = inputStyle,
            isError = state.emailError != null,
            errorText = state.emailError
        )

        Spacer(modifier = Modifier.height(10.dp))

        AuthPasswordField(
            value = state.password,
            onValueChange = { viewModel.onEvent(SignUpEvent.PasswordChanged(it)) },
            style = inputStyle.copy(label = "Password"),
            isError = state.passwordError != null,
            errorText = state.passwordError,
            passwordVisible = state.isPasswordVisible,
            onToggleVisibility = { viewModel.onEvent(SignUpEvent.TogglePasswordVisibility) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        AuthPasswordField(
            value = state.confirmPassword,
            onValueChange = { viewModel.onEvent(SignUpEvent.ConfirmPasswordChanged(it)) },
            style = inputStyle.copy(label = "Confirm Password"),
            isError = state.confirmPasswordError != null,
            errorText = state.confirmPasswordError,
            passwordVisible = state.isConfirmPasswordVisible,
            onToggleVisibility = { viewModel.onEvent(SignUpEvent.ToggleConfirmPasswordVisibility) }
        )

        Spacer(modifier = Modifier.height(32.dp))

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
            onGoogleClick = { coroutineScope.launch { googleAuthClient.signIn()?.let { viewModel.loginWithGoogle(it) } } },
            onFacebookClick = { triggerFacebookLogin() }
        )
        Spacer(modifier = Modifier.height(30.dp))
    }
}
