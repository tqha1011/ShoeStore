package com.example.shoestoreapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.shoestoreapp.core.networks.RetrofitInstance
import com.example.shoestoreapp.features.user.product.ui.product_detail.ProductDetailScreen
import com.example.shoestoreapp.features.user.product.ui.product_list.ProductListScreen
import com.example.shoestoreapp.features.user.cart.ui.screens.CartScreen
import com.example.shoestoreapp.features.user.cart.viewmodel.CartViewModel
import com.example.shoestoreapp.features.user.checkout.ui.screens.CheckoutScreen
import com.example.shoestoreapp.features.admin.invoice.ui.AdminInvoiceScreen
import com.example.shoestoreapp.features.admin.product.ui.components.AdminBottomNavTab
import com.example.shoestoreapp.features.user.product.ui.components.BottomNavTab
import com.example.shoestoreapp.features.user.product.viewmodel.ProductDetailViewModel
import com.example.shoestoreapp.features.user.product.viewmodel.ProductListViewModel
import com.example.shoestoreapp.features.user.invoice.ui.UserInvoiceScreen
import com.example.shoestoreapp.features.user.profile.ui.screens.UserProfileScreen
import com.example.shoestoreapp.features.admin.product.ui.screens.AdminProductListScreen
import com.example.shoestoreapp.features.admin.settings.ui.AdminSettingsScreen
import com.example.shoestoreapp.features.admin.voucher.ui.screen.VoucherManagementScreen
import com.example.shoestoreapp.features.admin.product.viewmodel.AdminProductListViewModel
import com.example.shoestoreapp.features.admin.addproduct.ui.AdminProductCrudScreen
import com.example.shoestoreapp.features.admin.addproduct.data.repositories.MasterDataRepository
import com.example.shoestoreapp.features.auth.presentation.reset_password.forgot_password.ForgotPasswordScreen
import com.example.shoestoreapp.features.auth.presentation.sign_in.LoginScreenContent
import com.example.shoestoreapp.features.auth.presentation.sign_up.RegisterScreenContent
import com.example.shoestoreapp.features.auth.presentation.welcome.WelcomeScreen
import com.example.shoestoreapp.features.auth.presentation.reset_password.create_new_password.CreateNewPasswordScreen
import com.example.shoestoreapp.core.utils.TokenManager
import com.example.shoestoreapp.features.admin.addproduct.viewmodel.AdminAddProductViewModel
import com.example.shoestoreapp.features.admin.addproduct.viewmodel.FetchMasterDataViewModel
import kotlinx.coroutines.launch

private object Routes {
    const val WELCOME = "welcome"
    const val SIGN_IN = "sign_in"
    const val SIGN_UP = "sign_up"
    const val FORGOT_PASSWORD = "forgot_password"
    const val CREATE_NEW_PASSWORD = "create_new_password/{email}/{otp}"
    const val PRODUCT_LIST = "product_list"
    const val PRODUCT_DETAIL = "product_detail/{productGuid}"
    const val CART = "cart"
    const val CHECKOUT = "checkout"
    const val USER_INVOICE_LIST = "user_invoice_list"
    const val USER_PROFILE = "user_profile"
    const val ADMIN_PRODUCT_LIST = "admin_product_list"
    const val ADMIN_CRUD = "admin_crud"
    const val ADMIN_EDIT_PRODUCT = "admin_edit_product/{productId}"
    const val ADMIN_INVOICE_LIST = "admin_invoice_list"
    const val ADMIN_SETTINGS = "admin_settings"
    const val ADMIN_VOUCHER_MANAGEMENT = "admin_voucher_management"

    fun createNewPassword(email: String, otp: String): String = "create_new_password/$email/$otp"
    fun adminEditProduct(productId: String): String = "admin_edit_product/$productId"
}

@Suppress("DEPRECATION")
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        RetrofitInstance.init(this)
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
        startDestination = Routes.WELCOME
    ) {
        authGraph(navController, tokenManager)
        userGraph(navController, tokenManager)
        adminGraph(navController, tokenManager)
    }
}

private fun NavGraphBuilder.authGraph(navController: NavHostController, tokenManager: TokenManager) {
    composable(Routes.WELCOME) {
        val token by tokenManager.getToken.collectAsState(initial = "LOADING")
        val role by tokenManager.getRole.collectAsState(initial = "")

        LaunchedEffect(token, role) {
            if (token == "LOADING") return@LaunchedEffect
            val destination = resolveWelcomeDestination(token, role)
            navController.navigateAndPopTo(destination, Routes.WELCOME)
        }

        WelcomeScreen(
            onNavigateToSignIn = {
                navController.navigateAndPopTo(Routes.SIGN_IN, Routes.WELCOME)
            }
        )
    }

    composable(Routes.SIGN_IN) {
        LoginScreenContent(
            onNavigateToSignUp = { navController.navigate(Routes.SIGN_UP) },
            onNavigateToForgotPassword = { navController.navigate(Routes.FORGOT_PASSWORD) },
            onNavigateToUserHome = {
                navController.navigateAndPopTo(Routes.PRODUCT_LIST, Routes.SIGN_IN)
            },
            onNavigateToAdminHome = {
                navController.navigateAndPopTo(Routes.ADMIN_PRODUCT_LIST, Routes.SIGN_IN)
            }
        )
    }

    composable(Routes.FORGOT_PASSWORD) {
        ForgotPasswordScreen(
            onNavigateCreateNewPassword = { email, otp ->
                navController.navigate(Routes.createNewPassword(email, otp))
            },
            onNavigateToSignIn = { navController.navigate(Routes.SIGN_IN) }
        )
    }

    composable(Routes.CREATE_NEW_PASSWORD) { backStackEntry ->
        val email = backStackEntry.arguments?.getString("email") ?: ""
        val otp = backStackEntry.arguments?.getString("otp") ?: ""

        CreateNewPasswordScreen(
            email = email,
            otp = otp,
            onNavigateToSignIn = { navController.navigate(Routes.SIGN_IN) }
        )
    }

    composable(Routes.SIGN_UP) {
        RegisterScreenContent(
            onNavigateToSignIn = { navController.navigate(Routes.SIGN_IN) },
            onNavigateToUserHome = {
                navController.navigateAndPopTo(Routes.PRODUCT_LIST, Routes.SIGN_UP)
            }
        )
    }
}

private fun NavGraphBuilder.userGraph(navController: NavHostController, tokenManager: TokenManager) {
    composable(Routes.PRODUCT_LIST) {
        ProductListScreen(
            viewModel = remember { ProductListViewModel() },
            onNavigateToDetail = { productGuid ->
                println("onNavigateToDetail called - productGuid: $productGuid")
                navController.navigate(Routes.PRODUCT_DETAIL.replace("{productGuid}", productGuid))
            },
            onTopMenuClick = { println("Menu clicked") },
            onNavigateToShoppingBag = { navController.navigate(Routes.CART) },
            onBottomTabSelected = { tab -> handleUserHomeTabSelection(tab, navController) }
        )
    }

    composable(Routes.CART) {
        CartScreen(
            viewModel = remember { CartViewModel() },
            onNavigateBack = { navController.popBackStack() },
            onNavigateToCheckout = { navController.navigate(Routes.CHECKOUT) },
            onNavigateToHome = {
                navController.navigateAndPopTo(Routes.PRODUCT_LIST, Routes.CART)
            },
            onNavigateToShop = {
                navController.navigateAndPopTo(Routes.PRODUCT_LIST, Routes.CART)
            },
            onNavigateToFavorites = { println("Favorites tab selected") },
            onNavigateToProfile = {
                navController.navigateAndPopTo(Routes.USER_PROFILE, Routes.CART)
            }
        )
    }

    composable(Routes.CHECKOUT) {
        CheckoutScreen(
            onBackClick = { navController.popBackStack() },
            onShoppingBagClick = { navController.navigateAndPopTo(Routes.CART, Routes.CHECKOUT) },
            onCompletePurchaseClick = {
                navController.navigateAndPopTo(Routes.PRODUCT_LIST, Routes.CHECKOUT)
            }
        )
    }

    composable(Routes.USER_INVOICE_LIST) {
        UserInvoiceScreen(
            onTabSelected = { tab -> handleUserInvoiceTabSelection(tab, navController) }
        )
    }

    composable(Routes.USER_PROFILE) {
        val scope = rememberCoroutineScope()

        UserProfileScreen(
            onTabSelected = { tab -> handleUserProfileTabSelection(tab, navController) },
            onLogoutClick = {
                scope.launch {
                    tokenManager.clearAuthInfo()
                    navController.navigateAfterLogout()
                }
            }
        )
    }

    composable(Routes.PRODUCT_DETAIL) { backStackEntry ->
        val productGuid = backStackEntry.arguments?.getString("productGuid") ?: "unknown"

        ProductDetailScreen(
            productGuid = productGuid,
            viewModel = remember { ProductDetailViewModel() },
            onBackClick = { navController.popBackStack() },
            onNavigateToCart = { navController.navigate(Routes.CART) }
        )
    }
}

private fun NavGraphBuilder.adminGraph(navController: NavHostController, tokenManager: TokenManager) {
    composable(Routes.ADMIN_PRODUCT_LIST) {
        AdminProductListScreen(
            viewModel = remember { AdminProductListViewModel() },
            onMenuClick = { println("Admin Menu clicked") },
            onAddProductClick = {
                navController.navigate(Routes.ADMIN_CRUD)
            },
            onTabSelected = { tab -> handleAdminProductTabSelection(tab, navController) },
            onEditProductClick = { productId ->
                navController.navigate(Routes.ADMIN_EDIT_PRODUCT.replace("{productId}", productId))
            },
            navController = navController
        )
    }

    composable(Routes.ADMIN_CRUD) {
        AdminProductCrudScreen(
            viewModel = remember {
                FetchMasterDataViewModel(
                    masterDataRepo = MasterDataRepository()
                )
            },
            addProductViewModel = remember { AdminAddProductViewModel() },
            onBackClick = { navController.popBackStack() },
            navController = navController
        )
    }

    composable(Routes.ADMIN_EDIT_PRODUCT) { backStackEntry ->
        val productId = backStackEntry.arguments?.getString("productId") ?: ""
        com.example.shoestoreapp.features.admin.product.ui.screens.AdminEditProductScreen(
            productId = productId,
            viewModel = viewModel(),
            onBackClick = { navController.popBackStack() }
        )
    }

    composable(Routes.ADMIN_INVOICE_LIST) {
        AdminInvoiceScreen(
            onTabSelected = { tab -> handleAdminInvoiceTabSelection(tab, navController) }
        )
    }

    composable(Routes.ADMIN_SETTINGS) {
        val scope = rememberCoroutineScope()

        AdminSettingsScreen(
            onTabSelected = { tab -> handleAdminSettingsTabSelection(tab, navController) },
            onLogoutClick = {
                scope.launch {
                    tokenManager.clearAuthInfo()
                    navController.navigateAfterLogout()
                }
            }
        )
    }

    composable(Routes.ADMIN_VOUCHER_MANAGEMENT) {
        VoucherManagementScreen(
            onTabSelected = { tab -> handleAdminVoucherTabSelection(tab, navController) }
        )
    }
}

private fun resolveWelcomeDestination(token: String?, role: String?): String = when {
    token.isNullOrEmpty() -> Routes.SIGN_IN
    role?.uppercase() == "ADMIN" -> Routes.ADMIN_PRODUCT_LIST
    else -> Routes.PRODUCT_LIST
}

private fun NavHostController.navigateAndPopTo(destination: String, popUpRoute: String) {
    navigate(destination) {
        popUpTo(popUpRoute) { inclusive = true }
    }
}

private fun NavHostController.navigateAfterLogout() {
    navigate(Routes.SIGN_IN) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}

private fun handleUserHomeTabSelection(tab: BottomNavTab, navController: NavHostController) {
    when (tab) {
        BottomNavTab.PROFILE -> navController.navigate(Routes.USER_PROFILE)
        BottomNavTab.BAG -> navController.navigate(Routes.CART)
        else -> Unit
    }
}

private fun handleUserInvoiceTabSelection(tab: BottomNavTab, navController: NavHostController) {
    when (tab) {
        BottomNavTab.HOME, BottomNavTab.SHOP -> {
            navController.navigateAndPopTo(Routes.PRODUCT_LIST, Routes.USER_INVOICE_LIST)
        }
        BottomNavTab.PROFILE -> {
            navController.navigateAndPopTo(Routes.USER_PROFILE, Routes.USER_INVOICE_LIST)
        }
        BottomNavTab.BAG -> Unit
        else -> println("User Tab selected: $tab")
    }
}

private fun handleUserProfileTabSelection(tab: BottomNavTab, navController: NavHostController) {
    when (tab) {
        BottomNavTab.HOME, BottomNavTab.SHOP -> {
            navController.navigateAndPopTo(Routes.PRODUCT_LIST, Routes.USER_PROFILE)
        }
        BottomNavTab.BAG -> {
            navController.navigateAndPopTo(Routes.CART, Routes.USER_PROFILE)
        }
        BottomNavTab.PROFILE -> Unit
        else -> println("User Tab selected: $tab")
    }
}

private fun handleAdminProductTabSelection(tab: AdminBottomNavTab, navController: NavHostController) {
    when (tab) {
        AdminBottomNavTab.ADMIN -> Unit
        AdminBottomNavTab.ORDERS -> navController.navigate(Routes.ADMIN_INVOICE_LIST)
        AdminBottomNavTab.VOUCHERS -> navController.navigate(Routes.ADMIN_VOUCHER_MANAGEMENT)
        AdminBottomNavTab.SETTINGS -> navController.navigate(Routes.ADMIN_SETTINGS)
        else -> println("Admin Tab selected: $tab")
    }
}

private fun handleAdminInvoiceTabSelection(tab: AdminBottomNavTab, navController: NavHostController) {
    when (tab) {
        AdminBottomNavTab.ADMIN -> {
            navController.navigateAndPopTo(Routes.ADMIN_PRODUCT_LIST, Routes.ADMIN_INVOICE_LIST)
        }
        AdminBottomNavTab.ORDERS -> Unit
        AdminBottomNavTab.VOUCHERS -> {
            navController.navigateAndPopTo(Routes.ADMIN_VOUCHER_MANAGEMENT, Routes.ADMIN_INVOICE_LIST)
        }
        AdminBottomNavTab.SETTINGS -> {
            navController.navigateAndPopTo(Routes.ADMIN_SETTINGS, Routes.ADMIN_INVOICE_LIST)
        }
        else -> println("Admin Tab selected: $tab")
    }
}

private fun handleAdminSettingsTabSelection(tab: AdminBottomNavTab, navController: NavHostController) {
    when (tab) {
        AdminBottomNavTab.ADMIN -> {
            navController.navigateAndPopTo(Routes.ADMIN_PRODUCT_LIST, Routes.ADMIN_SETTINGS)
        }
        AdminBottomNavTab.ORDERS -> {
            navController.navigateAndPopTo(Routes.ADMIN_INVOICE_LIST, Routes.ADMIN_SETTINGS)
        }
        AdminBottomNavTab.VOUCHERS -> {
            navController.navigateAndPopTo(Routes.ADMIN_VOUCHER_MANAGEMENT, Routes.ADMIN_SETTINGS)
        }
        AdminBottomNavTab.SETTINGS -> Unit
        else -> println("Admin Tab selected: $tab")
    }
}

private fun handleAdminVoucherTabSelection(tab: AdminBottomNavTab, navController: NavHostController) {
    when (tab) {
        AdminBottomNavTab.ADMIN -> {
            navController.navigateAndPopTo(Routes.ADMIN_PRODUCT_LIST, Routes.ADMIN_VOUCHER_MANAGEMENT)
        }
        AdminBottomNavTab.ORDERS -> {
            navController.navigateAndPopTo(Routes.ADMIN_INVOICE_LIST, Routes.ADMIN_VOUCHER_MANAGEMENT)
        }
        AdminBottomNavTab.VOUCHERS -> Unit
        AdminBottomNavTab.SETTINGS -> {
            navController.navigateAndPopTo(Routes.ADMIN_SETTINGS, Routes.ADMIN_VOUCHER_MANAGEMENT)
        }
        else -> println("Admin Tab selected: $tab")
    }
}
