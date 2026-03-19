package com.example.shoestoreapp.features.auth.ui.sign_up

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shoestoreapp.features.auth.ui.*
import com.example.shoestoreapp.features.auth.viewmodel.SignUpViewModel

@Composable
fun RegisterScreenContent(
    onNavigateToSignIn: () -> Unit = {},
    signUpViewModel: SignUpViewModel = viewModel()
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    LaunchedEffect(signUpViewModel.isSignUpSuccessful) {
        if (signUpViewModel.isSignUpSuccessful) {
            onNavigateToSignIn()
            signUpViewModel.resetState()
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
            // Use Template: Background (White for SignUp)
            AuthBackground(canvasColor = Color.White)

            // Use Template: TopBar
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

                // Use Template: Title
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

                // Use Template: Email Input
                AuthTextField(
                    value = signUpViewModel.email,
                    onValueChange = { signUpViewModel.onEmailChange(it) },
                    style = signUpInputStyle,
                    isError = signUpViewModel.emailError != null,
                    errorText = signUpViewModel.emailError
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Use Template: Password Input
                AuthPasswordField(
                    value = signUpViewModel.password,
                    onValueChange = { signUpViewModel.onPasswordChange(it) },
                    style = signUpPasswordStyle,
                    isError = signUpViewModel.passwordError != null,
                    errorText = signUpViewModel.passwordError,
                    passwordVisible = signUpViewModel.passwordVisible,
                    onToggleVisibility = { signUpViewModel.togglePasswordVisibility() }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Use Template: Confirm Password Input
                AuthPasswordField(
                    value = signUpViewModel.confirmPassword,
                    onValueChange = { signUpViewModel.onConfirmPasswordChange(it) },
                    style = signUpConfirmPasswordStyle,
                    isError = signUpViewModel.confirmPasswordError != null,
                    errorText = signUpViewModel.confirmPasswordError,
                    passwordVisible = signUpViewModel.confirmPasswordVisible,
                    onToggleVisibility = { signUpViewModel.toggleConfirmPasswordVisibility() }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Use Template: Action Button
                AuthActionButton(
                    text = "Sign up",
                    icon = Icons.Default.Key,
                    onClick = { signUpViewModel.onSignUpClick() },
                    containerColor = Color.White,
                    contentColor = Color.Black
                )

                Spacer(modifier = Modifier.weight(1f))

                // Use Template: Social Login Section
                SocialLoginSection(
                    dividerColor = Color.Black,
                    textColor = Color.Black,
                    buttonContainerColor = Color(0xFFF5F5F5),
                    iconTint = Color.Black
                )

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}