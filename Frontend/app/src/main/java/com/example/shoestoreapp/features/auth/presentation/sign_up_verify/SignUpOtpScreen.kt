package com.example.shoestoreapp.features.auth.presentation.sign_up_verify

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shoestoreapp.features.auth.presentation.components.AuthUiEvent
import com.example.shoestoreapp.features.auth.presentation.components.ResetPasswordContent
import com.example.shoestoreapp.features.auth.presentation.components.ResetPasswordTopBar
import com.example.shoestoreapp.features.auth.presentation.components.TitleBottom
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpOtpScreen(
    email: String,
    onNavigateToSignIn: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val viewModel: SignUpOtpViewModel = viewModel(
        key = "sign-up-otp-$email",
        factory = remember(email) {
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SignUpOtpViewModel(email = email) as T
                }
            }
        }
    )
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            if (event is AuthUiEvent.NavigateToSignIn) {
                onNavigateToSignIn()
            }
        }
    }

    Scaffold(
        topBar = { ResetPasswordTopBar(onBackClick = onBackClick) },
        containerColor = Color.White
    ) { paddingValues ->
        SignUpOtpContent(
            email = email,
            state = state,
            onEvent = viewModel::onEvent,
            paddingValues = paddingValues
        )
    }
}

@Composable
private fun SignUpOtpContent(
    email: String,
    state: SignUpOtpState,
    onEvent: (SignUpOtpEvent) -> Unit,
    paddingValues: PaddingValues
) {
    ResetPasswordContent(
        paddingValues = paddingValues,
        title = "Verify Email",
        description = "We sent a 6-digit verification code to $email. Enter it below to finish creating your account."
    ) {
        SignUpOtpInput(state = state, onEvent = onEvent)

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                if (!state.isLoading) onEvent(SignUpOtpEvent.Submit)
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
                text = if (state.isLoading) "Verifying..." else "Verify Code",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        TitleBottom()
    }
}

@Composable
private fun SignUpOtpInput(
    state: SignUpOtpState,
    onEvent: (SignUpOtpEvent) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = state.otpCode,
            onValueChange = { input ->
                if (input.length <= 6 && input.all { it.isDigit() }) {
                    onEvent(SignUpOtpEvent.OtpChanged(input))
                }
            },
            label = { Text("Verification Code", color = Color.Black) },
            placeholder = { Text("Enter your verification code", color = Color.LightGray) },
            modifier = Modifier.fillMaxWidth(),
            isError = state.otpError != null,
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

        if (state.otpError != null) {
            Text(
                text = state.otpError,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }
    }
}
