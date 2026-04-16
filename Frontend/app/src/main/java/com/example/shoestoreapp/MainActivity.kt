package com.example.shoestoreapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.shoestoreapp.core.networks.RetrofitInstance
import com.example.shoestoreapp.features.user.product.ui.product_detail.ProductDetailScreen
import com.example.shoestoreapp.features.user.product.ui.product_list.ProductListScreen
import com.example.shoestoreapp.features.user.product.viewmodel.ProductDetailViewModel
import com.example.shoestoreapp.features.user.product.viewmodel.ProductListViewModel
import com.example.shoestoreapp.features.cart.ui.screens.CartScreen
import com.example.shoestoreapp.features.cart.viewmodel.CartViewModel
import com.example.shoestoreapp.features.checkout.ui.screens.CheckoutScreen
import com.example.shoestoreapp.features.checkout.viewmodel.CheckoutViewModel
import com.example.shoestoreapp.features.admin.product.ui.AdminProductListScreen
import com.example.shoestoreapp.features.admin.product.viewmodel.AdminProductListViewModel
import com.example.shoestoreapp.features.admin.crud.ui.AdminProductCrudScreen
import com.example.shoestoreapp.features.admin.crud.viewmodel.ProductCrudViewModel
import com.example.shoestoreapp.features.admin.crud.data.repositories.ProductCrudRepository
import com.example.shoestoreapp.features.auth.presentation.reset_password.forgot_password.ForgotPasswordScreen
import com.example.shoestoreapp.features.auth.presentation.sign_in.LoginScreenContent
import com.example.shoestoreapp.features.auth.presentation.sign_up.RegisterScreenContent
import com.example.shoestoreapp.features.auth.presentation.welcome.WelcomeScreen
import com.example.shoestoreapp.features.auth.presentation.reset_password.create_new_password.CreateNewPasswordScreen
import com.example.shoestoreapp.core.utils.TokenManager
import com.example.shoestoreapp.features.admin.crud.data.repositories.MasterDataRepository
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RetrofitInstance.init(this)
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
        startDestination = "welcome"
        //startDestination = "product_list"  // ← Test ProductListScreen
        //startDestination = "admin_product_list"  // Test AdminProductListScreen
    ) {

        // Route 1: Welcome Screen
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
                // Pass email and OTP to next screen via URL or bundle
                onNavigateCreateNewPassword = { email, otp ->
                    navController.navigate("create_new_password/$email/$otp")
                },
                onNavigateToSignIn = {
                    navController.navigate("sign_in")
                },
            )
        }

        // Route 2.2: Create New Password
        composable("create_new_password/{email}/{otp}") {
                backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            val otp = backStackEntry.arguments?.getString("otp") ?: ""
            CreateNewPasswordScreen(
                email = email,
                otp = otp,
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

//        // Route 4: User Home
//        composable("home_user") {
//            // 1. CREATE A COROUTINE SCOPE TO RUN SUSPEND FUNCTIONS
//            val coroutineScope = rememberCoroutineScope()
//
//            HomeUserScreen(
//                onLogoutClick = {
//                    // 2. LAUNCH A BACKGROUND TASK TO CLEAR DATASTORE
//                    coroutineScope.launch {
//                        // Clear Token and Role from the local storage
//                        tokenManager.clearAuthInfo()
//
//                        // 3. NAVIGATE BACK TO SIGN IN AND CLEAR ENTIRE BACKSTACK
//                        navController.navigate("sign_in") {
//                            // popUpTo(0) means clearing all previous screens
//                            // so the user cannot press the physical Back button to return to Home
//                            popUpTo(0) { inclusive = true }
//                        }
//                    }
//                }
//            )
//        }

        // Route: Product List Screen
        composable("product_list") {
            ProductListScreen(
                viewModel = remember { ProductListViewModel() },
                onNavigateToDetail = { productGuid ->  // ← Changed: productGuid is String GUID
                    println("🟢 onNavigateToDetail called - productGuid: $productGuid")
                    navController.navigate("product_detail/$productGuid")  // ← Pass GUID directly
                },
                onTopMenuClick = {
                    println("🔹 Menu clicked")
                },
                onNavigateToShoppingBag = {
                    navController.navigate("cart")
                }
            )
        }

        // Route: Product Detail Screen
        composable("product_detail/{productGuid}") { backStackEntry ->
            // ← Changed: Get productGuid as String (GUID), not Int
            val productGuid = backStackEntry.arguments?.getString("productGuid") ?: ""

            ProductDetailScreen(
                productGuid = productGuid,  // ← String GUID
                viewModel = remember { ProductDetailViewModel() },
                onBackClick = {
                    // Click back -> quay lại ProductListScreen
                    navController.popBackStack()
                },
                onNavigateToCart = {
                    navController.navigate("cart")
                }
            )
        }

        // Route: Cart Screen
        composable("cart") {
            CartScreen(
                viewModel = remember { CartViewModel() },
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToCheckout = {
                    navController.navigate("checkout")
                }
            )
        }

        // Route: Checkout Screen
        composable("checkout") {
            CheckoutScreen(
                checkoutViewModel = remember { CheckoutViewModel() },
                onBackClick = {
                    navController.popBackStack()
                },
                onShoppingBagClick = {
                    println("🔹 Checkout Shopping Bag clicked")
                },
                onEditAddressClick = {
                    println("🔹 Edit Address clicked")
                },
                onCompletePurchaseClick = {
                    println("✅ Purchase completed!")
                    // TODO: Navigate to confirmation screen or home after successful purchase
                    navController.navigate("home_user") {
                        popUpTo("checkout") { inclusive = true }
                    }
                }
            )
        }
        
        // Route: Admin Product Screen
        composable("admin_product_list") {
            AdminProductListScreen(
                viewModel = remember { AdminProductListViewModel() },
                onMenuClick = {
                    println("🔹 Admin Menu clicked")
                },
                onAddProductClick = {
                    println("🔹 Add Product clicked")
                    navController.navigate("admin_product_crud")
                },
                onTabSelected = { tab ->
                    println("🔹 Admin Tab selected: $tab")
                }
            )
        }

        // Route: Admin CRUD Screen
        composable("admin_product_crud") {
            AdminProductCrudScreen(
                viewModel = remember { ProductCrudViewModel(ProductCrudRepository(),
                    MasterDataRepository()
                ) },
                onBackClick = {
                    navController.popBackStack()
                },
            )
        }

        // Route 5: Admin Home
//        composable("home_admin") {
//            val coroutineScope = rememberCoroutineScope()
//            HomeAdminScreen( onLogoutClick = {
//                coroutineScope.launch {
//                    tokenManager.clearAuthInfo()
//
//                    navController.navigate("sign_in") {
//                        popUpTo(0) { inclusive = true }
//                    }
//                }
//            })
//        }
    }
}