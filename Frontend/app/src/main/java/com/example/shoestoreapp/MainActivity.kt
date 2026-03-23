package com.example.shoestoreapp

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.shoestoreapp.features.auth.presentation.reset_password.forgot_password.ForgotPasswordScreen
import com.example.shoestoreapp.features.auth.presentation.sign_in.LoginScreenContent
import com.example.shoestoreapp.features.auth.presentation.sign_up.RegisterScreenContent
import com.example.shoestoreapp.features.auth.presentation.welcome.WelcomeScreen
import com.example.shoestoreapp.features.auth.presentation.reset_password.create_new_password.CreateNewPasswordScreen
import com.example.shoestoreapp.features.auth.HomeUserScreen
import com.example.shoestoreapp.features.auth.HomeAdminScreen
import com.example.shoestoreapp.core.utils.TokenManager
import kotlinx.coroutines.launch
import java.security.MessageDigest


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
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }


    NavHost(
        navController = navController,
        startDestination = "welcome" // Welcome Screen
    ) {
        // Route 1: Welcome Screen (Acts as Splash Screen)
        composable("welcome") {
            // 1. COLLECT DATA FROM FLOW AS STATE
            val token by tokenManager.getToken.collectAsState(initial = "LOADING")
            val role by tokenManager.getRole.collectAsState(initial = "")

            // 2. AUTO-NAVIGATION LOGIC
            LaunchedEffect(token, role) {
                // Guard clause: Early return to prevent deep nesting (SonarCloud Fix)
                if (token == "LOADING") return@LaunchedEffect

                // Wait for 1 second to let users see the Welcome UI properly
                kotlinx.coroutines.delay(1000)

                // Flattened conditional logic using 'when' statement
                val destination = when {
                    token.isNullOrEmpty() -> "sign_in"
                    role?.uppercase() == "ADMIN" -> "home_admin"
                    else -> "home_user"
                }

                // Execute single navigation call
                navController.navigate(destination) {
                    popUpTo("welcome") { inclusive = true }
                }
            }
            // 3. RENDER THE WELCOME UI
            WelcomeScreen(
                onNavigateToSignIn = {
                    // Manual fallback just in case the user clicks the button
                    // before the 1-second delay finishes
                    navController.navigate("sign_in") {
                        popUpTo("welcome") { inclusive = true }
                    }
                }
            )
        }

        // Route 2: Sign-in Screen
        composable("sign_in") {
            LoginScreenContent(
                onNavigateToSignUp = {
                    navController.navigate("sign_up")
                },
                onNavigateToForgotPassword = {
                    navController.navigate("forgot_password")
                },
                onNavigateToUserHome = {
                    navController.navigate("home_user") {
                        popUpTo("sign_in") { inclusive = true }
                    }
                },
                onNavigateToAdminHome = {
                    navController.navigate("home_admin") {
                        popUpTo("sign_in") { inclusive = true }
                    }
                }
            )
        }

        // Route 2.1: Forgot Password
        composable("forgot_password") {
            ForgotPasswordScreen(
                onNavigateCreateNewPassword = {
                    navController.navigate("create_new_password")
                },
                onNavigateToSignIn = {
                    navController.navigate("sign_in")
                },
            )
        }

        // Route 2.2: Create New Password
        composable("create_new_password") {
            CreateNewPasswordScreen(
                onNavigateToSignIn = {
                    navController.navigate("sign_in")
                }
            )
        }

        // Route 3: Sign-up
        composable("sign_up") {
            RegisterScreenContent(
                onNavigateToSignIn = {
                    navController.navigate("sign_in")
                },
                onNavigateToUserHome = {
                    navController.navigate("home_user")
                },
            )
        }

        // Route 4: User Home
        composable("home_user") {
            // 1. CREATE A COROUTINE SCOPE TO RUN SUSPEND FUNCTIONS
            val coroutineScope = rememberCoroutineScope()

            HomeUserScreen(
                onLogoutClick = {
                    // 2. LAUNCH A BACKGROUND TASK TO CLEAR DATASTORE
                    coroutineScope.launch {
                        // Clear Token and Role from the local storage
                        tokenManager.clearAuthInfo()

                        // 3. NAVIGATE BACK TO SIGN IN AND CLEAR ENTIRE BACKSTACK
                        navController.navigate("sign_in") {
                            // popUpTo(0) means clearing all previous screens
                            // so the user cannot press the physical Back button to return to Home
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

        // Route 5: Admin Home
        composable("home_admin") {
            val coroutineScope = rememberCoroutineScope()
            HomeAdminScreen( onLogoutClick = {
                coroutineScope.launch {
                    tokenManager.clearAuthInfo()

                    navController.navigate("sign_in") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            })
        }
    }
}