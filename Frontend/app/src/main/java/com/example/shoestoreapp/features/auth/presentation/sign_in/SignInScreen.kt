package com.example.shoestoreapp.features.auth.presentation.sign_in

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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

@Composable
fun LoginScreenContent(
    onNavigateToSignUp: () -> Unit = {},
    onNavigateToForgotPassword: () -> Unit = {},
    // UPDATED: Replace onNavigateToHomeScreen with 2 routes: Admin and User
    onNavigateToUserHome: () -> Unit = {},
    onNavigateToAdminHome: () -> Unit = {}
) {
    // 1. Get Context to use DataStore
    val context = LocalContext.current

    // 2. Create TokenManager (token storage handler)
    val tokenManager = remember { TokenManager(context) }

    // 3. Initialize ViewModel using a manual Factory to pass tokenManager
    val viewModel: SignInViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SignInViewModel(tokenManager = tokenManager) as T
            }
        }
    )

    val coroutineScope = rememberCoroutineScope()

    val googleAuthClient = remember {
        com.example.shoestoreapp.core.utils.GoogleAuthClient(context)
    }

    // 4. Get State from ViewModel to render UI
    val state by viewModel.state.collectAsState()

    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    // 5. Listen to navigation events (route switching) from ViewModel
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is SignInViewModel.UiEvent.NavigateToUserHome -> {
                    onNavigateToUserHome()
                }
                is SignInViewModel.UiEvent.NavigateToAdminHome -> {
                    onNavigateToAdminHome()
                }
                is SignInViewModel.UiEvent.NavigateToSignUp -> {
                    onNavigateToSignUp()
                }
                is SignInViewModel.UiEvent.ShowError -> {
                    // Future implementation: Show Snackbar with error message
                }
            }
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(800)) + slideInVertically(
            initialOffsetY = { 100 },
            animationSpec = tween(800)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
            AuthBackground(canvasColor = Color.Black)

            AuthTopBar(
                buttonText = "Sign up",
                onButtonClick = onNavigateToSignUp,
                contentColor = Color.Black,
                buttonContainerColor = Color.White
            )

            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 35.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(1.8f))

                TitleText("Sign In", color = Color.Black)

                Spacer(modifier = Modifier.weight(2f))

                val signInInputStyle = AuthFieldStyle(
                    label = "Email",
                    containerColor = Color(0xFF222222),
                    textColor = Color.White
                )

                val signInPasswordStyle = AuthFieldStyle(
                    label = "Password",
                    containerColor = Color(0xFF222222),
                    textColor = Color.White
                )

                // Pass state and onEvent into input fields
                AuthTextField(
                    value = state.email,
                    onValueChange = { viewModel.onEvent(SignInEvent.EmailChanged(it)) },
                    style = signInInputStyle,
                    isError = state.emailError != null,
                    errorText = state.emailError
                )

                Spacer(modifier = Modifier.height(20.dp))

                AuthPasswordField(
                    value = state.password,
                    onValueChange = { viewModel.onEvent(SignInEvent.PasswordChanged(it)) },
                    style = signInPasswordStyle,
                    isError = state.passwordError != null,
                    errorText = state.passwordError,
                    passwordVisible = state.isPasswordVisible,
                    onToggleVisibility = {
                        viewModel.onEvent(SignInEvent.TogglePasswordVisibility)
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Update UI based on loading state
                AuthActionButton(
                    text = if (state.isLoading) "Loading..." else "Sign in",
                    icon = Icons.Default.Key,
                    onClick = {
                        if (!state.isLoading) {
                            viewModel.onEvent(SignInEvent.Submit)
                        }
                    },
                    containerColor = Color(0xFF1A1A1A),
                    contentColor = Color.White
                )

                Text(
                    text = "Forgot password?",
                    fontSize = 16.sp,
                    color = Color.LightGray,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 8.dp)
                        .clickable { onNavigateToForgotPassword() }
                )

                Spacer(modifier = Modifier.weight(1f))

                SocialLoginSection(
                    dividerColor = Color.DarkGray,
                    textColor = Color.DarkGray,
                    buttonContainerColor = Color(0xFF222222),
                    iconTint = Color.White,
                    onGoogleClick = {
                        // When clicking Google, launch coroutine to open account picker
                        coroutineScope.launch {
                            val idToken = googleAuthClient.signIn()
                            if (idToken != null) {
                                // Got the token → send it to backend
                                viewModel.loginWithGoogle(idToken)
                            } else {
                                // User canceled or error occurred → you can show a Toast here
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}