package com.example.shoestoreapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.shoestoreapp.features.auth.ui.auth.login.ForgotPassword
import com.example.shoestoreapp.features.auth.ui.auth.login.LoginScreenContent
import com.example.shoestoreapp.features.auth.ui.auth.register.RegisterScreenContent
import com.example.shoestoreapp.features.auth.ui.welcome.WelcomeScreen
import com.example.shoestoreapp.features.auth.ui.auth.login.CreateNewPassword

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
                onNavigateToSignIn = {
                    navController.navigate("sign_in") {
                        // Xóa màn hình welcome khỏi backstack để khi nhấn back không quay lại welcome nữa
                        popUpTo("welcome") { inclusive = true }
                    }
                }
            )
        }

        // Route 2: Sign in Screen
        composable("sign_in") {
            LoginScreenContent (
                onNavigateToSignUp = {
                navController.navigate("sign_up")
                },
                onNavigateToForgotPassword = {
                    navController.navigate("forgot_password")
                },
            )
        }

        // Route 2.1: Forgot Password Screen
        composable ("forgot_password") {
            ForgotPassword(
                onNavigateCreateNewPassword = {
                    navController.navigate("create_new_password")
                },
                onNavigateToSignIn = {
                    navController.navigate("sign_in")
                },
            )
        }

        // Route 2.2: Create New Password Screen
        composable("create_new_password") {
            CreateNewPassword(
                onNavigateToSignIn = {
                    navController.navigate("sign_in")
                }
            )
        }

        // Route 3: Sign up Screen
        composable("sign_up") {
            RegisterScreenContent(
                onNavigateToSignIn = {
                    navController.navigate("sign_in")
                }
            )
        }
    }
}
