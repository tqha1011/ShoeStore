package com.example.shoestoreapp.features.auth.ui.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shoestoreapp.R

@Composable
fun WelcomeScreen(
    onNavigateToSignIn: () -> Unit
) {
    // Lock navigate
    var hasNavigated by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    // dragAmount < 0 : Swipe up

                    if (dragAmount < -50 && !hasNavigated) {
                        hasNavigated = true
                        onNavigateToSignIn()
                    }
                }
            }
    ) {
        // Background fill full screen
        Image(
            painter = painterResource(id = R.drawable.bg_welcome),
            contentDescription = "Background Welcome",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}