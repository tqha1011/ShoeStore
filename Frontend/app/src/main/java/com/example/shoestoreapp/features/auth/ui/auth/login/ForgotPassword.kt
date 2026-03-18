package com.example.shoestoreapp.features.auth.ui.auth.login

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
import androidx.compose.ui.Alignment
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
    Scaffold(
        topBar = {
            ResetPasswordTopBar(onBackClick = onNavigateToSignIn)
        },
    ) { paddingValues ->
        ResetPasswordContent(
            paddingValues = paddingValues,
            title = "Forgot Password",
            description = if (!viewModel.isCodeSent) 
                "Please enter your email address. We will send you a verification code to reset your password."
            else 
                "Verification code has been sent! Please check your email and enter the code below."
        ) {
            // Input Email
            OutlinedTextField(
                value = viewModel.email,
                onValueChange = { viewModel.onEmailChange(it) },
                label = { Text("Email Address", color = Color.Black) },
                placeholder = { Text("yourname@gmail.com", color = Color.LightGray) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !viewModel.isCodeSent, // Lock input email when code is sent
                isError = viewModel.emailError != null,
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
            
            if (viewModel.emailError != null) {
                Text(
                    text = viewModel.emailError ?: "",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.Start).padding(start = 8.dp, top = 4.dp)
                )
            }

          // when isCodeSent true, show verification code input
            AnimatedVisibility(
                visible = viewModel.isCodeSent,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(20.dp))
                    OutlinedTextField(
                        value = viewModel.verificationCode,
                        onValueChange = { viewModel.onVerificationCodeChange(it) },
                        label = { Text("Verification Code", color = Color.Black) },
                        placeholder = { Text("Enter your verification code", color = Color.LightGray) },
                        modifier = Modifier.fillMaxWidth(),
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
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Submit Button
            Button(
                onClick = {
                    if (!viewModel.isCodeSent) {
                       // First click : Sent verify code
                        viewModel.sendRequest()
                    } else {
                        // Second click : Navigate CreateNewPassword page
                        onNavigateCreateNewPassword()
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
            
            Text(
                text = "Shoe Store App",
                color = Color.LightGray,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 20.dp)
            )
        }
    }
}