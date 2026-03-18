package com.example.shoestoreapp.features.auth.ui.auth.register

import com.example.shoestoreapp.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.sharp.Dashboard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Key
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shoestoreapp.features.auth.viewmodel.RegisterViewModel


@Composable
fun RegisterScreenContent(
    onNavigateToSignIn: () -> Unit = {},
    registerViewModel: RegisterViewModel = viewModel()
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(800)) + slideInVertically(
            initialOffsetY = { 100 },
            animationSpec = tween(800)
        )
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)) {
            RegisterBackgroundCanvas()
            RegisterTopButtonActions(onNavigateToSignIn = onNavigateToSignIn)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 35.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(4f))
                Text(
                    "Sign Up",
                    fontSize = 45.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Normal
                )
                Spacer(modifier = Modifier.weight(4f))

                // Email
                Text(
                    "Email",
                    fontSize = 17.sp,
                    color = Color.Gray,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 8.dp)
                )
                OutlinedTextField(
                    value = registerViewModel.email,
                    onValueChange = { registerViewModel.onEmailChange(it) },
                    isError = registerViewModel.emailError != null,
                    supportingText = { registerViewModel.emailError?.let { Text(it) } },
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF5F5F5),
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        errorContainerColor = Color(0xFFF5F5F5)
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Password
                Text(
                    "Password",
                    fontSize = 17.sp,
                    color = Color.Gray,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 8.dp)
                )
                PasswordInputField(
                    password = registerViewModel.password,
                    onPasswordChange = { registerViewModel.onPasswordChange(it) },
                    passwordVisible = passwordVisible,
                    onToggleVisibility = { passwordVisible = !passwordVisible },
                    isError = registerViewModel.passwordError != null,
                    errorText = registerViewModel.passwordError
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Confirm Password
                Text(
                    "Confirm Password",
                    fontSize = 17.sp,
                    color = Color.Gray,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 8.dp)
                )
                ConfirmPasswordInputField(
                    confirmPassword = registerViewModel.confirmPassword,
                    onConfirmPasswordChange = { registerViewModel.onConfirmPasswordChange(it) },
                    confirmPasswordVisible = confirmPasswordVisible,
                    onToggleVisibility = { confirmPasswordVisible = !confirmPasswordVisible },
                    isError = registerViewModel.confirmPasswordError != null,
                    errorText = registerViewModel.confirmPasswordError
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Button Sign Up
                Button(
                    onClick = {
                        registerViewModel.onSignUpClick {
                            onNavigateToSignIn() // Navigate page when success
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp)
                        .border(
                            border = BorderStroke(
                                2.dp,
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFFFE8F33),
                                        Color(0xFFC246DE)
                                    )
                                )
                            ),
                            shape = RoundedCornerShape(50.dp)
                        ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(50.dp)
                ) {
                    Icon(Icons.Default.Key, contentDescription = null)
                    Spacer(modifier = Modifier.width(5.dp))
                    Text("Sign up", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.weight(1f))

                // Social Login Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.Black)
                    Text(
                        "Or continue with",
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.DarkGray)
                }

                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    SocialLoginButton(R.drawable.ic_google) {}
                    Spacer(modifier = Modifier.width(20.dp))
                    SocialLoginButton(R.drawable.ic_facebook) {}
                    Spacer(modifier = Modifier.width(20.dp))
                    SocialLoginButton(R.drawable.ic_tiktok) {}
                }
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
fun PasswordInputField(
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onToggleVisibility: () -> Unit,
    isError: Boolean,
    errorText: String?
) {
    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        isError = isError,
        supportingText = { errorText?.let { Text(it) } },
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = onToggleVisibility) {
                Icon(
                    if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                    contentDescription = null
                )
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF5F5F5),
            unfocusedContainerColor = Color(0xFFF5F5F5),
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            errorContainerColor = Color(0xFFF5F5F5)
        )
    )
}

@Composable
fun ConfirmPasswordInputField(
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    confirmPasswordVisible: Boolean,
    onToggleVisibility: () -> Unit,
    isError: Boolean,
    errorText: String?
) {
    OutlinedTextField(
        value = confirmPassword,
        onValueChange = onConfirmPasswordChange,
        isError = isError,
        supportingText = { errorText?.let { Text(it) } },
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = onToggleVisibility) {
                Icon(
                    if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                    contentDescription = null
                )
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF5F5F5),
            unfocusedContainerColor = Color(0xFFF5F5F5),
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            errorContainerColor = Color(0xFFF5F5F5)
        )
    )
}

@Composable
fun RegisterBackgroundCanvas() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val yBoundary = size.height / 3.8f
        val path = Path().apply {
            moveTo(0f, size.height)
            lineTo(0f, yBoundary)
            cubicTo(
                size.width / 2f,
                yBoundary - 150f,
                size.width / 2f,
                yBoundary + 450f,
                size.width,
                yBoundary + 100f
            )
            lineTo(size.width, size.height)
            close()
        }
        drawPath(path, Color.White)
    }
}

@Composable
fun RegisterTopButtonActions(onNavigateToSignIn: () -> Unit) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding()) {
        IconButton(
            onClick = {},
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 5.dp)
        ) {
            Icon(Icons.Sharp.Dashboard, contentDescription = null, tint = Color.White)
        }
        Button(
            onClick = onNavigateToSignIn,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.Gray
            ),
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = "Sign up",
                modifier = Modifier.size(25.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                "Sign in",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }
    }
}

@Composable
fun SocialLoginButton(iconRes: Int, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .background(Color(0xFFF5F5F5), RoundedCornerShape(10.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(painterResource(iconRes), contentDescription = null, modifier = Modifier.size(35.dp))
    }
}
