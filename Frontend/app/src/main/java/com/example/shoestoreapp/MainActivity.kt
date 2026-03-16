package com.example.shoestoreapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.shoestoreapp.features.auth.ui.auth.login.LoginScreenContent
import com.example.shoestoreapp.features.auth.ui.auth.register.RegisterScreenContent

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
    // Initialize the NavController to manage app navigation
    val navController = rememberNavController()

    // Setup the NavHost and set the starting destination to "login"
    NavHost(
        navController = navController,
        startDestination = "sign in"
    ) {

        // Route 1: Login Screen
        composable("sign in") {
            LoginScreenContent(
                onNavigateToSignUp = {
                    // Navigate to the register screen when the Sign Up button is clicked
                    navController.navigate("sign up")
                }
            )
        }

        // Route 2: Register Screen
        composable("sign up") {
            RegisterScreenContent (
                onNavigateToSignIn = {
                    navController.navigate("sign in")
                }
            )
        }
    }
}