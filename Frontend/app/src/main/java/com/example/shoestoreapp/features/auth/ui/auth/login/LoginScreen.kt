package com.example.shoestoreapp.features.auth.ui.auth.login

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
import androidx.compose.material.icons.filled.Key
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.clickable


// Main UI Content
@Composable
fun LoginScreenContent(
    onNavigateToSignUp: () -> Unit = {}, // Navigate to sign up
    onNavigateToForgotPassword: () -> Unit = {}, // Navigate to forgot password
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }

    // Run animation on screen launch
    LaunchedEffect(Unit) {
        isVisible = true // Đổi state sang true để bắt đầu vẽ hiệu ứng
    }

    //
    // Surf Animation
    //
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(
            // show up on 800 milisecond
            animationSpec = tween(durationMillis = 800)
        ) + slideInVertically(
            // slide down to up 100 pixel
            initialOffsetY = { 100 },
            animationSpec = tween(durationMillis = 800)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            //=============================
            // BackGround White - Black
            //============================
            LoginBackgroundCanvas();
            LoginTopButtonActions(onNavigateToSignUp = onNavigateToSignUp);
            // Top Left Dashboard Icon


            // ===============================
            // MAIN CONTENT
            //================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 35.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.weight(1.8f))


                Text(
                    "Sign In",
                    fontSize = 45.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Normal
                )
                Spacer(modifier = Modifier.weight(2f))
                // Email Field
                Text(
                    "Email",
                    fontSize = 17.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 8.dp)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    shape = RoundedCornerShape(size = 20.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF222222),
                        unfocusedContainerColor = Color(0xFF222222),
                        focusedBorderColor = Color.Gray,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.LightGray,
                        cursorColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(20.dp))

                // Password Field
                Text(
                    "Password",
                    fontSize = 17.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 8.dp)
                )
                // Call Function Password
                PasswordInputField(
                    password = password,
                    onPasswordChange = { password = it },
                    passwordVisible = passwordVisible,
                    onToggleVisibility = { passwordVisible = !passwordVisible }
                )


                Spacer(modifier = Modifier.height(32.dp))

                // Main Gradient Login Button
                Button(
                    onClick = { /* Handle Login logic here */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp)
                        .border(
                            border = BorderStroke(
                                width = 2.dp,
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFFE8F33), // Orange
                                        Color(0xFFC246DE)  // Purple
                                    )
                                )
                            ),
                            shape = RoundedCornerShape(50.dp)
                        ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1A1A1A),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(50.dp)
                ) {
                    Icon(
                        Icons.Default.Key,
                        contentDescription = "Sign In"
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Sign in",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Forgot Password Link
                Text(
                    text = "Forgot password?",
                    fontSize = 16.sp,
                    color = Color.LightGray,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 8.dp)
                        .clickable {onNavigateToForgotPassword()}
                )

                Spacer(modifier = Modifier.weight(1f))

                // Divider line "Or continue with"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.DarkGray)
                    Text(
                        text = "Or continue with",
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.DarkGray)
                }

                Spacer(modifier = Modifier.height(30.dp))

                // Social Login Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    SocialLoginButton(
                        iconRes = R.drawable.ic_google,
                        contentDescription = "Login with Google",
                        onClick = { /* Handle Google Login */ }
                    )

                    Spacer(modifier = Modifier.width(20.dp))

                    SocialLoginButton(
                        iconRes = R.drawable.ic_facebook,
                        contentDescription = "Login with Facebook",
                        onClick = { /* Handle Facebook Login */ }
                    )

                    Spacer(modifier = Modifier.width(20.dp))

                    SocialLoginButton(
                        iconRes = R.drawable.ic_tiktok,
                        contentDescription = "Login with TikTok",
                        onClick = { /* Handle TikTok Login */ }
                    )
                }
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
// Function Password
@Composable
fun PasswordInputField(
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onToggleVisibility: () -> Unit
) {

    val visualTrans = if (passwordVisible) {
        VisualTransformation.None
    } else {
        PasswordVisualTransformation()
    }

    val iconImage = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
    val iconDesc = if (passwordVisible) "Hide password" else "Show password"

    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        shape = RoundedCornerShape(size = 20.dp),
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = visualTrans,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password
        ),
        trailingIcon = {
            IconButton(onClick = onToggleVisibility) {
                Icon(
                    imageVector = iconImage,
                    contentDescription = iconDesc,
                    tint = Color.Gray
                )
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF222222),
            unfocusedContainerColor = Color(0xFF222222),
            focusedBorderColor = Color.Gray,
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.LightGray,
            cursorColor = Color.White
        )
    )
}

// Function BackgroudCanvas
@Composable
fun LoginBackgroundCanvas() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val yBoundary = height / 3.8f

        // Create path for curved background
        val path = Path().apply {
            moveTo(0f, height) // Bottom-left corner
            lineTo(0f, yBoundary) // Draw line up

            cubicTo(
                x1 = width / 2f,
                y1 = yBoundary - 150f,  // Control point 1 (Top-left curve)
                x2 = width / 2f,
                y2 = yBoundary + 450f,  // Control point 2 (Top-right curve)
                x3 = width,
                y3 = yBoundary + 100f,  // End point at right edge
            )

            lineTo(width, height) // Draw line down to bottom-right
            close()
        }
        drawPath(
            path = path,
            color = Color.Black,
        )
    }
}
//Function two button in top
@Composable
fun LoginTopButtonActions(
    onNavigateToSignUp: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        IconButton(
            onClick = { /* Handle click event */ },
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            ),
            modifier = Modifier
                .padding(start = 5.dp)
                .statusBarsPadding()
                .align(Alignment.TopStart)
        ) {
            Icon(
                Icons.Sharp.Dashboard,
                contentDescription = null,
                Modifier.size(30.dp)
            )
        }

        // Top Right Sign Up Button
        Button(
            onClick = { onNavigateToSignUp() },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Gray
            ),
            modifier = Modifier
                .statusBarsPadding()
                .align(Alignment.TopEnd) // Align to top right
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = "Sign up",
                modifier = Modifier.size(25.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                "Sign up",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }
    }
}

// Function SocialLoginButton
@Composable
fun SocialLoginButton(
    iconRes : Int,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .background(Color(0xFF222222), shape = RoundedCornerShape(10.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ){
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(35.dp)
        )
    }
}
