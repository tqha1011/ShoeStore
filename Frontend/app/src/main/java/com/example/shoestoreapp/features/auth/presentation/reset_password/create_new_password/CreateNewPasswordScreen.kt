package com.example.shoestoreapp.features.auth.presentation.reset_password.create_new_password

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shoestoreapp.features.auth.presentation.components.AuthUiEvent // IMPORTANT: Using the shared AuthUiEvent
import com.example.shoestoreapp.features.auth.presentation.components.ResetPasswordContent
import com.example.shoestoreapp.features.auth.presentation.components.ResetPasswordTopBar
import com.example.shoestoreapp.features.auth.presentation.components.TitleBottom
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateNewPasswordScreen(
    email: String, // REQUIRED: Received from ForgotPasswordScreen via NavHost
    otp: String,   // REQUIRED: Received from ForgotPasswordScreen via NavHost
    onNavigateToSignIn: () -> Unit = {},
    viewModel: CreateNewPasswordViewModel = viewModel()
) {
    // 1. Collect UI State
    val state by viewModel.state.collectAsState()

    // 2. Initialize credentials in ViewModel when screen opens
    LaunchedEffect(email, otp) {
        viewModel.initCredentials(email, otp)
    }

    // 3. Listen for shared Navigation Events
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is AuthUiEvent.NavigateToSignIn -> {
                    onNavigateToSignIn()
                }
                is AuthUiEvent.ShowError -> {
                    // Future implementation: Show Snackbar with event.message
                }
                else -> Unit // Exhaustive when to satisfy SonarCloud
            }
        }
    }

    Scaffold(
        topBar = {
            ResetPasswordTopBar(onBackClick = onNavigateToSignIn)
        },
        containerColor = Color.White
    ) { paddingValues ->
        ResetPasswordContent(
            paddingValues = paddingValues,
            title = "Create New Password",
            description = "Your new password must be different from previously used passwords."
        ) {
            // New Password Input Field
            NewPasswordInput(
                state = state,
                onEvent = viewModel::onEvent
            )

            // Confirm Password Input Field
            ConfirmPasswordInput(
                state = state,
                onEvent = viewModel::onEvent
            )

            // Display Error Message if any
            ErrorDisplay(errorText = state.passwordError)

            Spacer(modifier = Modifier.height(40.dp))

            // Submit Button
            ResetPasswordButton(
                isLoading = state.isLoading,
                onEvent = viewModel::onEvent
            )

            Spacer(modifier = Modifier.weight(1f))

            TitleBottom()
        }
    }
}

@Composable
fun ErrorDisplay(errorText: String?) {
    if (errorText != null) {
        Column {
            Text(
                text = errorText,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 8.dp, top = 8.dp)
            )
        }
    }
}

@Composable
fun ResetPasswordButton(
    isLoading: Boolean,
    onEvent: (CreateNewPasswordEvent) -> Unit
) {
    Button(
        onClick = { if (!isLoading) onEvent(CreateNewPasswordEvent.Submit) },
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Black,
            contentColor = Color.White
        )
    ) {
        Text(
            text = if (isLoading) "Loading..." else "Reset Password",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ConfirmPasswordInput(
    state: CreateNewPasswordState,
    onEvent: (CreateNewPasswordEvent) -> Unit
) {
    OutlinedTextField(
        value = state.confirmPassword,
        onValueChange = { onEvent(CreateNewPasswordEvent.ConfirmPasswordChanged(it)) },
        label = { Text("Confirm Password", color = Color.Gray) },
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = if (state.isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        leadingIcon = {
            Icon(Icons.Default.Key, contentDescription = null, tint = Color.Black)
        },
        trailingIcon = {
            IconButton(onClick = { onEvent(CreateNewPasswordEvent.ToggleConfirmPasswordVisibility) }) {
                Icon(
                    imageVector = if (state.isConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = null,
                    tint = Color.Black
                )
            }
        },
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Black,
            unfocusedBorderColor = Color.Black.copy(alpha = 0.3f),
            focusedLabelColor = Color.Black,
            cursorColor = Color.Black
        )
    )
}

@Composable
fun NewPasswordInput(
    state: CreateNewPasswordState,
    onEvent: (CreateNewPasswordEvent) -> Unit
) {
    OutlinedTextField(
        value = state.password,
        onValueChange = { onEvent(CreateNewPasswordEvent.PasswordChanged(it)) },
        label = { Text("New Password", color = Color.Gray) },
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        leadingIcon = {
            Icon(Icons.Default.Key, contentDescription = null, tint = Color.Black)
        },
        trailingIcon = {
            IconButton(onClick = { onEvent(CreateNewPasswordEvent.TogglePasswordVisibility) }) {
                Icon(
                    imageVector = if (state.isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = null,
                    tint = Color.Black
                )
            }
        },
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Black,
            unfocusedBorderColor = Color.Black.copy(alpha = 0.3f),
            focusedLabelColor = Color.Black,
            cursorColor = Color.Black
        )
    )

    Spacer(modifier = Modifier.height(20.dp))
}