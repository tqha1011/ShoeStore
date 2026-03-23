package com.example.shoestoreapp.features.auth.presentation.sign_up

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shoestoreapp.core.utils.TokenManager
import com.example.shoestoreapp.features.auth.presentation.components.*
import androidx.lifecycle.ViewModel
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
    val googleAuthClient = remember {
        com.example.shoestoreapp.core.utils.GoogleAuthClient(context)
    }

    // 1. Collect state from ViewModel
    val state by viewModel.state.collectAsState()

    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    // 2. Listen for one-time events (navigation) from ViewModel
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is SignUpViewModel.UiEvent.NavigateToSignIn -> {
                    onNavigateToSignIn()
                }
                is SignUpViewModel.UiEvent.NavigateToUserHome -> {
                    // Handle successful Google login
                    onNavigateToUserHome()
                }
                is SignUpViewModel.UiEvent.ShowError -> {
                    // Future implementation: Show Snackbar
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
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            AuthBackground(canvasColor = Color.White)

            AuthTopBar(
                buttonText = "Sign in",
                onButtonClick = onNavigateToSignIn,
                contentColor = Color.White,
                buttonContainerColor = Color.Black
            )

            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 35.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(4f))

                TitleText("Sign Up", color = Color.White)

                Spacer(modifier = Modifier.weight(4f))

                val signUpInputStyle = AuthFieldStyle(
                    label = "Email",
                    containerColor = Color(0xFFF5F5F5),
                    textColor = Color.Black,
                    unfocusedBorderColor = Color.LightGray
                )

                val signUpPasswordStyle = AuthFieldStyle(
                    label = "Password",
                    containerColor = Color(0xFFF5F5F5),
                    textColor = Color.Black,
                    unfocusedBorderColor = Color.LightGray
                )

                val signUpConfirmPasswordStyle = AuthFieldStyle(
                    label = "Confirm Password",
                    containerColor = Color(0xFFF5F5F5),
                    textColor = Color.Black,
                    unfocusedBorderColor = Color.LightGray
                )

                // 3. Pass state down and trigger events up
                AuthTextField(
                    value = state.email,
                    onValueChange = { viewModel.onEvent(SignUpEvent.EmailChanged(it)) },
                    style = signUpInputStyle,
                    isError = state.emailError != null,
                    errorText = state.emailError
                )

                Spacer(modifier = Modifier.height(10.dp))

                AuthPasswordField(
                    value = state.password,
                    onValueChange = { viewModel.onEvent(SignUpEvent.PasswordChanged(it)) },
                    style = signUpPasswordStyle,
                    isError = state.passwordError != null,
                    errorText = state.passwordError,
                    passwordVisible = state.isPasswordVisible,
                    onToggleVisibility = {
                        viewModel.onEvent(SignUpEvent.TogglePasswordVisibility)
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                AuthPasswordField(
                    value = state.confirmPassword,
                    onValueChange = { viewModel.onEvent(SignUpEvent.ConfirmPasswordChanged(it)) },
                    style = signUpConfirmPasswordStyle,
                    isError = state.confirmPasswordError != null,
                    errorText = state.confirmPasswordError,
                    passwordVisible = state.isConfirmPasswordVisible,
                    onToggleVisibility = {
                        viewModel.onEvent(SignUpEvent.ToggleConfirmPasswordVisibility)
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Update UI based on loading state
                AuthActionButton(
                    text = if (state.isLoading) "Loading..." else "Sign up",
                    icon = Icons.Default.Key,
                    onClick = {
                        if (!state.isLoading) {
                            viewModel.onEvent(SignUpEvent.Submit)
                        }
                    },
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
                        // When clicking Google, launch coroutine to open account picker
                        coroutineScope.launch {
                            val idToken = googleAuthClient.signIn()
                            if (idToken != null) {
                                viewModel.loginWithGoogle(idToken)
                            } else {
                                // User canceled or error occurred
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}