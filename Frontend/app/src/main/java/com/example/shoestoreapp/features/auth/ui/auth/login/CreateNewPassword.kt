package com.example.shoestoreapp.features.auth.ui.auth.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shoestoreapp.features.auth.viewmodel.CreateNewPasswordModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateNewPassword(
    onNavigateToSignIn: () -> Unit = {},
    viewModel: CreateNewPasswordModel = viewModel()
) {
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
            // New Password Input
            NewPasswordInput(viewModel = viewModel)

            // Confirm Password Input
            ConfirmPasswordInput(viewModel = viewModel)


            // Error Display
            if (viewModel.passwordError != null) {
                Text(
                    text = viewModel.passwordError ?: "",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 8.dp, top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Reset Password Button
            ResetPasswordButton(viewModel = viewModel, onNavigateToSignIn = onNavigateToSignIn)

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

@Composable
fun ResetPasswordButton(viewModel: CreateNewPasswordModel, onNavigateToSignIn: () -> Unit) {
    Button(
        onClick = {
            if (viewModel.validateAndSubmit()) {
                // Success - navigate back to sign in
                onNavigateToSignIn()
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
            text = "Reset Password",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ConfirmPasswordInput(viewModel: CreateNewPasswordModel) {
    OutlinedTextField(
        value = viewModel.confirmPassword,
        onValueChange = { viewModel.onConfirmPasswordChange(it) },
        label = { Text("Confirm Password", color = Color.Gray) },
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = if (viewModel.confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        leadingIcon = {
            Icon(Icons.Default.Key, contentDescription = null, tint = Color.Black)
        },
        trailingIcon = {
            IconButton(onClick = { viewModel.toggleConfirmPasswordVisibility() }) {
                Icon(
                    imageVector = if (viewModel.confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
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
fun NewPasswordInput(viewModel: CreateNewPasswordModel) {
    OutlinedTextField(
        value = viewModel.password,
        onValueChange = { viewModel.onPasswordChange(it) },
        label = { Text("New Password", color = Color.Gray) },
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = if (viewModel.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        leadingIcon = {
            Icon(Icons.Default.Key, contentDescription = null, tint = Color.Black)
        },
        trailingIcon = {
            IconButton(onClick = { viewModel.togglePasswordVisibility() }) {
                Icon(
                    imageVector = if (viewModel.passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
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