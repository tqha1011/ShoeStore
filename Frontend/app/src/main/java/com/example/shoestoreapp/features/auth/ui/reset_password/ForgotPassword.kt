package com.example.shoestoreapp.features.auth.ui.reset_password

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
import com.example.shoestoreapp.features.auth.viewmodel.ForgotPasswordModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPassword(
    onNavigateCreateNewPassword: () -> Unit = {},
    onNavigateToSignIn: () -> Unit = {},
    viewModel: ForgotPasswordModel = viewModel()
) {
    // Listen for verification success from ViewModel to navigate
    LaunchedEffect(viewModel.isVerificationSuccessful) {
        if (viewModel.isVerificationSuccessful) {
            onNavigateCreateNewPassword()
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            ResetPasswordTopBar(
                onBackClick = onNavigateToSignIn
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        ResetPasswordContent(
            paddingValues = paddingValues,
            title = "Forgot Password",
            description = if (!viewModel.isCodeSent)
                "Please enter your email address. We will send you a verification code to reset your password."
            else
                "Verification code has been sent! Please check your email and enter the code below."
        ) {
            // Email Input Section
            EmailInputField(
                email = viewModel.email,
                onValueChange = { viewModel.onEmailChange(it) },
                label = "Email",
                isCodeSent = viewModel.isCodeSent,
                isError = viewModel.emailError != null,
                errorText = viewModel.emailError
            )

            // Verification Code Input Section (Visible when isCodeSent is true)
            VerificationCodeInput(
                isCodeSent = viewModel.isCodeSent,
                verificationCode = viewModel.verificationCode,
                onValueChange = { viewModel.onVerificationCodeChange(it) },
                label = "Verification Code",
                verificationError = viewModel.verificationError
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Submit Button
            Button(
                onClick = {
                    if (!viewModel.isCodeSent) {
                        // First click: Send verification code
                        viewModel.sendRequest()
                    } else {
                        // Second click: Verify code
                        viewModel.verifyCode()
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
                Text(
                    text = if (!viewModel.isCodeSent) "Send Request" else "Confirm Code",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            TitleBottom()
        }
    }
}

@Composable
fun VerificationCodeInput(
    isCodeSent: Boolean,
    verificationCode: String,
    onValueChange: (String) -> Unit,
    label: String,
    verificationError: String?
) {
    AnimatedVisibility(
        visible = isCodeSent,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedTextField(
                value = verificationCode,
                onValueChange = onValueChange,
                label = { Text(label, color = Color.Black) },
                placeholder = { Text("Enter your verification code", color = Color.LightGray) },
                modifier = Modifier.fillMaxWidth(),
                isError = verificationError != null,
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

            if (verificationError != null) {
                Text(
                    text = verificationError,
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
    email: String,
    onValueChange: (String) -> Unit,
    label: String,
    isCodeSent: Boolean,
    isError: Boolean,
    errorText: String?
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = email,
            onValueChange = onValueChange,
            label = { Text(label, color = Color.Black) },
            placeholder = { Text("yourname@gmail.com", color = Color.LightGray) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isCodeSent,
            isError = isError,
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

        if (isError && errorText != null) {
            Text(
                text = errorText,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }
    }
}