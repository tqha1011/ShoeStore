package com.example.shoestoreapp.features.auth.presentation.reset_password.forgot_password

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shoestoreapp.features.auth.presentation.common.AuthUiEvent
import com.example.shoestoreapp.features.auth.presentation.components.ResetPasswordContent
import com.example.shoestoreapp.features.auth.presentation.components.ResetPasswordTopBar
import com.example.shoestoreapp.features.auth.presentation.components.TitleBottom
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onNavigateCreateNewPassword: (String, String) -> Unit = { _, _ -> }, // Accepts 2 parameters: email and otp
    onNavigateToSignIn: () -> Unit = {},
    viewModel: ForgotPasswordViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is AuthUiEvent.NavigateToCreateNewPassword -> {
                    // Pass email and otp to navigation graph
                    onNavigateCreateNewPassword(event.email, event.otp)
                }
                is AuthUiEvent.ShowError -> {
                    // Show Snackbar logic here
                }
                else -> Unit
            }
        }
    }

    Scaffold(
        topBar = { ResetPasswordTopBar(onBackClick = onNavigateToSignIn) },
        containerColor = Color.White
    ) { paddingValues ->
        ForgotPasswordForm(
            state = state,
            onEvent = viewModel::onEvent,
            paddingValues = paddingValues
        )
    }
}
@Composable
fun ForgotPasswordForm(
    state: ForgotPasswordState,
    onEvent: (ForgotPasswordEvent) -> Unit,
    paddingValues: PaddingValues
) {
    val descriptionText = if (!state.isCodeSent) {
        "Please enter your email address. We will send you a verification code to reset your password."
    } else {
        "Verification code has been sent! Please check your email and enter the code below."
    }

    ResetPasswordContent(
        paddingValues = paddingValues,
        title = "Forgot Password",
        description = descriptionText
    ) {
        EmailInputField(state = state, onEvent = onEvent)

        VerificationCodeInput(state = state, onEvent = onEvent)

        Spacer(modifier = Modifier.height(40.dp))

        ForgotPasswordSubmitButton(state = state, onEvent = onEvent)

        Spacer(modifier = Modifier.weight(1f))

        TitleBottom()
    }
}
@Composable
fun ForgotPasswordSubmitButton(
    state: ForgotPasswordState,
    onEvent: (ForgotPasswordEvent) -> Unit
) {
    Button(
        onClick = {
            if (!state.isLoading) {
                if (!state.isCodeSent) {
                    onEvent(ForgotPasswordEvent.SubmitEmail)
                } else {
                    onEvent(ForgotPasswordEvent.SubmitCode)
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Black,
            contentColor = Color.White
        )
    ) {
        val buttonText = when {
            state.isLoading -> "Loading..."
            !state.isCodeSent -> "Send Request"
            else -> "Confirm Code"
        }

        Text(
            text = buttonText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
@Composable
fun VerificationCodeInput(
    state: ForgotPasswordState,
    onEvent: (ForgotPasswordEvent) -> Unit
) {
    AnimatedVisibility(
        visible = state.isCodeSent,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedTextField(
                value = state.verificationCode,
                onValueChange = {input ->
                    // Limit number
                    if (input.length <= 6 && input.all { it.isDigit() }) {
                        onEvent(ForgotPasswordEvent.VerificationCodeChanged(input))
                    }},
                label = { Text("Verification Code", color = Color.Black) },
                placeholder = { Text("Enter your verification code", color = Color.LightGray) },
                modifier = Modifier.fillMaxWidth(),
                isError = state.verificationError != null,
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Black)
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

            if (state.verificationError != null) {
                Text(
                    text = state.verificationError,
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun EmailInputField(
    state: ForgotPasswordState,
    onEvent: (ForgotPasswordEvent) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = state.email,
            onValueChange = { onEvent(ForgotPasswordEvent.EmailChanged(it)) },
            label = { Text("Email", color = Color.Black) },
            placeholder = { Text("yourname@gmail.com", color = Color.LightGray) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isCodeSent,
            isError = state.emailError != null,
            leadingIcon = {
                Icon(Icons.Default.Email, contentDescription = null, tint = Color.Black)
            },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Black.copy(alpha = 0.3f),
                focusedLabelColor = Color.Black,
                cursorColor = Color.Black,
                disabledBorderColor = Color.Black.copy(alpha = 0.1f)
            )
        )

        if (state.emailError != null) {
            Text(
                text = state.emailError,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }
    }
}
