package com.example.shoestoreapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.shoestoreapp.features.auth.ui.auth.login.LoginScreenContent
import com.example.shoestoreapp.features.auth.ui.auth.register.RegisterScreenContent
import com.example.shoestoreapp.features.auth.ui.welcome.WelcomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                AppNavHost()
            }
        }
    }
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    // Đặt startDestination là "welcome" để hiện màn hình chào đầu tiên
    NavHost(
        navController = navController,
        startDestination = "welcome"
    ) {
        // Route 1: Welcome Screen
        composable("welcome") {
            WelcomeScreen(
                onNavigateToLogin = {
                    navController.navigate("sign in") {
                        // Xóa màn hình welcome khỏi backstack để khi nhấn back không quay lại welcome nữa
                        popUpTo("welcome") { inclusive = true }
                    }
                }
            )
        }

        // Route 2: Sign in Screen
        composable("sign in") {
            LoginScreenContent(
                onNavigateToSignUp = {
                    navController.navigate("sign up")
                }
            )
        }

        // Route 3: Sign up Screen
        composable("sign up") {
            RegisterScreenContent(
                onNavigateToSignIn = {
                    navController.navigate("sign in")
                }
            )
        }
    }
}
