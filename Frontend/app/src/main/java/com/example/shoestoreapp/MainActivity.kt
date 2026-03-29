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
import com.example.shoestoreapp.features.auth.ui.reset_password.ForgotPassword
import com.example.shoestoreapp.features.auth.ui.sign_in.LoginScreenContent
import com.example.shoestoreapp.features.auth.ui.sign_up.RegisterScreenContent
import com.example.shoestoreapp.features.auth.ui.welcome.WelcomeScreen
import com.example.shoestoreapp.features.auth.ui.reset_password.CreateNewPassword
import com.example.shoestoreapp.features.user.product.ui.product_detail.ProductDetailScreen
import com.example.shoestoreapp.features.user.product.ui.product_list.ProductListScreen
import com.example.shoestoreapp.features.user.product.viewmodel.ProductDetailViewModel
import com.example.shoestoreapp.features.user.product.viewmodel.ProductListViewModel

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
        //startDestination = "welcome"
        startDestination = "product_list"  // ← Test ProductListScreen
    ) {

        // Route 1: Welcome Screen
        composable("welcome") {
            WelcomeScreen(
                onNavigateToSignIn = {
                    navController.navigate("sign_in") {
                        // Delete Welcome Screen 
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

        // Route: Product List Screen
        composable("product_list") {
            ProductListScreen(
                viewModel = remember { ProductListViewModel() },
                onNavigateToDetail = { productId ->
                    println("🟢 onNavigateToDetail called - productId: $productId")
                    navController.navigate("product_detail/$productId")
                },
                onTopMenuClick = {
                    println("🔹 Menu clicked")
                },
                onNavigateToShoppingBag = {
                    println("🔹 Shopping bag clicked")
                }
            )
        }

        // Route: Product Detail Screen
        composable("product_detail/{productId}") { backStackEntry ->
            // Lấy productId từ URL
            val productId = backStackEntry.arguments?.getString("productId")?.toInt() ?: 1

            ProductDetailScreen(
                productId = productId,
                viewModel = remember { ProductDetailViewModel() },
                onBackClick = {
                    // Click back -> quay lại ProductListScreen
                    navController.popBackStack()
                },
                onNavigateToCart = {
                    // Click "Add to Cart" -> điều hướng sang Cart screen
                    println("🔹 Navigating to cart")
                    // navController.navigate("cart")
                }
            )
        }
    }
}
