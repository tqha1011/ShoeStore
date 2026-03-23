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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shoestoreapp.core.utils.TokenManager
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
    val googleAuthClient = remember {
        com.example.shoestoreapp.core.utils.GoogleAuthClient(context)
    }

    // Collect state from ViewModel
    val state by viewModel.state.collectAsState()
    var isVisible by remember { mutableStateOf(false) }

    // Trigger enter animation
    LaunchedEffect(Unit) {
        isVisible = true
    }

    // Listen for navigation events from ViewModel
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is SignUpViewModel.UiEvent.NavigateToSignIn -> onNavigateToSignIn()
                is SignUpViewModel.UiEvent.NavigateToUserHome -> onNavigateToUserHome()
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
        // Pass the entire 'state' object to keep parameter count strictly under Sonar's limits
        SignUpUI(
            state = state,
            onEvent = viewModel::onEvent,
            onNavigateToSignIn = onNavigateToSignIn,
            onGoogleSignUpClick = {
                coroutineScope.launch {
                    googleAuthClient.signIn()?.let { viewModel.loginWithGoogle(it) }
                }
            }
        )
    }
}

@Composable
private fun SignUpUI(
    state: SignUpState, // Ensure this matches your actual state class name
    onEvent: (SignUpEvent) -> Unit,
    onNavigateToSignIn: () -> Unit,
    onGoogleSignUpClick: () -> Unit
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

            val inputStyle = AuthFieldStyle(
                label = "Email",
                containerColor = Color(0xFFF5F5F5),
                textColor = Color.Black,
                unfocusedBorderColor = Color.LightGray
            )

            // Email Field
            AuthTextField(
                value = state.email,
                onValueChange = { onEvent(SignUpEvent.EmailChanged(it)) },
                style = inputStyle,
                isError = state.emailError != null,
                errorText = state.emailError
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Password Field
            AuthPasswordField(
                value = state.password,
                onValueChange = { onEvent(SignUpEvent.PasswordChanged(it)) },
                style = inputStyle.copy(label = "Password"), // Reuse styling cleanly
                isError = state.passwordError != null,
                errorText = state.passwordError,
                passwordVisible = state.isPasswordVisible,
                onToggleVisibility = { onEvent(SignUpEvent.TogglePasswordVisibility) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Confirm Password Field
            AuthPasswordField(
                value = state.confirmPassword,
                onValueChange = { onEvent(SignUpEvent.ConfirmPasswordChanged(it)) },
                style = inputStyle.copy(label = "Confirm Password"), // Reuse styling cleanly
                isError = state.confirmPasswordError != null,
                errorText = state.confirmPasswordError,
                passwordVisible = state.isConfirmPasswordVisible,
                onToggleVisibility = { onEvent(SignUpEvent.ToggleConfirmPasswordVisibility) }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Update UI based on loading state
            AuthActionButton(
                text = if (state.isLoading) "Loading..." else "Sign up",
                icon = Icons.Default.Key,
                onClick = {
                    if (!state.isLoading) onEvent(SignUpEvent.Submit)
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
                onGoogleClick = onGoogleSignUpClick
            )

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}