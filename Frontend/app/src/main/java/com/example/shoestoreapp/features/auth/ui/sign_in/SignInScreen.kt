package com.example.shoestoreapp.features.auth.ui.sign_in

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shoestoreapp.features.auth.ui.*
import com.example.shoestoreapp.features.auth.viewmodel.SignInViewModel

@Composable
fun LoginScreenContent(
    onNavigateToSignUp: () -> Unit = {},
    onNavigateToForgotPassword: () -> Unit = {},
    onNavigateToHomeScreen: () -> Unit = {},
    signInViewModel: SignInViewModel = viewModel()
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    LaunchedEffect(signInViewModel.isSignInSuccessful) {
        if (signInViewModel.isSignInSuccessful) {
            onNavigateToHomeScreen()
            signInViewModel.resetState()
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
            // Use Template: Background
            AuthBackground(canvasColor = Color.Black)

            // Use Template: TopBar
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
                
                // Use Template: Title
                TitleText("Sign In", color = Color.Black)
                
                Spacer(modifier = Modifier.weight(2f))
                // Class Style Auth
                val signInInputStyle = AuthFieldStyle(
                    label = "Email",
                    containerColor = Color(0xFF222222),
                    textColor = Color.White
                )
                // Class Style Auth
                val signInPasswordStyle = AuthFieldStyle(
                    label = "Password",
                    containerColor = Color(0xFF222222),
                    textColor = Color.White
                )
                // Use Template: Email Input
                AuthTextField(
                    value = signInViewModel.email,
                    onValueChange = { signInViewModel.onEmailChange(it) },
                    style = signInInputStyle,
                    isError = signInViewModel.emailError != null,
                    errorText = signInViewModel.emailError
                )
                Spacer(modifier = Modifier.height(20.dp))
                
                // Use Template: Password Input
                AuthPasswordField(
                    value = signInViewModel.password,
                    onValueChange = { signInViewModel.onPasswordChange(it) },
                    style = signInPasswordStyle,
                    isError = signInViewModel.passwordError != null,
                    errorText = signInViewModel.passwordError,
                    passwordVisible = signInViewModel.passwordVisible,
                    onToggleVisibility = { signInViewModel.onTogglePasswordVisibility() }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Use Template: Action Button
                AuthActionButton(
                    text = "Sign in",
                    icon = Icons.Default.Key,
                    onClick = { signInViewModel.onSignInClick() },
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

                // Use Template: Social Login Section
                SocialLoginSection(
                    dividerColor = Color.DarkGray,
                    textColor = Color.DarkGray,
                    buttonContainerColor = Color(0xFF222222),
                    iconTint = Color.White
                )
                
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}