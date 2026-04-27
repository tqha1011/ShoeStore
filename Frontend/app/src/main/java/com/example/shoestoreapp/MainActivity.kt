package com.example.shoestoreapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.shoestoreapp.core.networks.RetrofitInstance
import com.example.shoestoreapp.features.user.product.ui.product_detail.ProductDetailScreen
import com.example.shoestoreapp.features.user.product.ui.product_list.ProductListScreen
import com.example.shoestoreapp.features.admin.invoice.ui.AdminInvoiceScreen
import com.example.shoestoreapp.features.admin.invoice.data.AdminInvoiceRepository
import com.example.shoestoreapp.features.admin.invoice.viewmodel.AdminInvoiceViewmodel
import com.example.shoestoreapp.features.admin.product.ui.components.AdminBottomNavTab
import com.example.shoestoreapp.features.user.product.ui.components.BottomNavTab
import com.example.shoestoreapp.features.user.product.viewmodel.ProductDetailViewModel
import com.example.shoestoreapp.features.user.product.viewmodel.ProductListViewModel
import com.example.shoestoreapp.features.user.invoice.ui.UserInvoiceScreen
import com.example.shoestoreapp.features.user.invoice.viewmodel.UserInvoiceViewModel
import com.example.shoestoreapp.features.user.profile.ui.UserProfileScreen
import com.example.shoestoreapp.features.admin.product.ui.AdminProductListScreen
import com.example.shoestoreapp.features.admin.settings.ui.AdminSettingsScreen
import com.example.shoestoreapp.features.admin.product.viewmodel.AdminProductListViewModel
import com.example.shoestoreapp.features.admin.crud.ui.AdminProductCrudScreen
import com.example.shoestoreapp.features.admin.crud.viewmodel.ProductCrudViewModel
import com.example.shoestoreapp.features.admin.crud.data.repositories.ProductCrudRepository
import com.example.shoestoreapp.features.admin.crud.data.repositories.MasterDataRepository
import com.example.shoestoreapp.features.auth.presentation.reset_password.forgot_password.ForgotPasswordScreen
import com.example.shoestoreapp.features.auth.presentation.sign_in.LoginScreenContent
import com.example.shoestoreapp.features.auth.presentation.sign_up.RegisterScreenContent
import com.example.shoestoreapp.features.auth.presentation.welcome.WelcomeScreen
import com.example.shoestoreapp.features.auth.presentation.reset_password.create_new_password.CreateNewPasswordScreen
import com.example.shoestoreapp.core.utils.TokenManager
import com.example.shoestoreapp.core.utils.JwtUtils
import com.example.shoestoreapp.features.user.invoice.data.UserInvoiceRepository
import com.example.shoestoreapp.features.invoice.model.InvoiceStatus
import kotlinx.coroutines.launch

private object Routes {
    const val WELCOME = "welcome"
    const val SIGN_IN = "sign_in"
    const val SIGN_UP = "sign_up"
    const val FORGOT_PASSWORD = "forgot_password"
    const val CREATE_NEW_PASSWORD = "create_new_password/{email}/{otp}"
    const val PRODUCT_LIST = "product_list"
    const val PRODUCT_DETAIL = "product_detail/{productGuid}"
    const val USER_INVOICE_LIST_BASE = "user_invoice_list"
    const val USER_INVOICE_LIST = "$USER_INVOICE_LIST_BASE?status={status}"
    const val USER_PROFILE = "user_profile"
    const val ADMIN_PRODUCT_LIST = "admin_product_list"
    const val ADMIN_CRUD = "admin_crud"
    const val ADMIN_INVOICE_LIST = "admin_invoice_list"
    const val ADMIN_SETTINGS = "admin_settings"

    fun createNewPassword(email: String, otp: String): String = "create_new_password/$email/$otp"
    fun userInvoiceList(status: InvoiceStatus? = null): String {
        return if (status == null) USER_INVOICE_LIST_BASE else "$USER_INVOICE_LIST_BASE?status=${status.name}"
    }
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

            // check expired token and redirect to login screen
            if (!token.isNullOrBlank() && JwtUtils.isTokenExpired(token)) {
                tokenManager.clearAuthInfo()
                navController.navigateAndPopTo(Routes.SIGN_IN, Routes.WELCOME)
                return@LaunchedEffect
            }

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
            onNavigateToShoppingBag = { println("Shopping bag clicked") },
            onBottomTabSelected = { tab -> handleUserHomeTabSelection(tab, navController) }
        )
    }

    composable(
        route = Routes.USER_INVOICE_LIST,
        arguments = listOf(
            navArgument("status") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) { backStackEntry ->
        val statusArg = backStackEntry.arguments?.getString("status")
        val initialStatus = statusArg?.let { raw ->
            runCatching { InvoiceStatus.valueOf(raw) }.getOrNull()
        }
        val userInvoiceViewModel = remember {
            UserInvoiceViewModel(UserInvoiceRepository(RetrofitInstance.invoiceApi))
        }

        UserInvoiceScreen(
            viewModel = userInvoiceViewModel,
            initialStatus = initialStatus,
            onTabSelected = { tab -> handleUserInvoiceTabSelection(tab, navController) }
        )
    }

    composable(Routes.USER_PROFILE) {
        val scope = rememberCoroutineScope()
        val userInvoiceViewModel = remember {
            UserInvoiceViewModel(UserInvoiceRepository(RetrofitInstance.invoiceApi))
        }

        UserProfileScreen(
            onTabSelected = { tab -> handleUserProfileTabSelection(tab, navController) },
            onViewOrdersClick = {
                navController.navigate(Routes.userInvoiceList())
            },
            onLogoutClick = {
                scope.launch {
                    tokenManager.clearAuthInfo()
                    navController.navigateAfterLogout()
                }
            },
            orderCounts = userInvoiceViewModel.state.orderCounts,
            onStatusClick = { status ->
                navController.navigate(Routes.userInvoiceList(status))
            }
        )
    }

    composable(Routes.PRODUCT_DETAIL) { backStackEntry ->
        val productGuid = backStackEntry.arguments?.getString("productGuid") ?: "unknown"

        ProductDetailScreen(
            productGuid = productGuid,
            viewModel = remember { ProductDetailViewModel() },
            onBackClick = { navController.popBackStack() },
            onNavigateToCart = { println("Navigating to cart") }
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
            navController = navController
        )
    }

    composable(Routes.ADMIN_CRUD) {
        AdminProductCrudScreen(
            viewModel = remember {
                ProductCrudViewModel(
                    repository = ProductCrudRepository(),
                    masterDataRepo = MasterDataRepository()
                )
            },
            onBackClick = { navController.popBackStack() },
            navController = navController
        )
    }

    composable(Routes.ADMIN_INVOICE_LIST) {
        AdminInvoiceScreen(
            viewModel = remember {
                AdminInvoiceViewmodel(
                    repository = AdminInvoiceRepository(RetrofitInstance.invoiceApi)
                )
            },
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
        BottomNavTab.BAG -> Unit
        else -> Unit
    }
}

private fun handleUserInvoiceTabSelection(tab: BottomNavTab, navController: NavHostController) {
    when (tab) {
        BottomNavTab.HOME, BottomNavTab.SHOP -> {
            navController.navigateAndPopTo(Routes.PRODUCT_LIST, Routes.USER_INVOICE_LIST_BASE)
        }
        BottomNavTab.PROFILE -> {
            navController.navigateAndPopTo(Routes.USER_PROFILE, Routes.USER_INVOICE_LIST_BASE)
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
        BottomNavTab.BAG -> Unit
        BottomNavTab.PROFILE -> Unit
        else -> println("User Tab selected: $tab")
    }
}

private fun handleAdminProductTabSelection(tab: AdminBottomNavTab, navController: NavHostController) {
    when (tab) {
        AdminBottomNavTab.ADMIN -> Unit
        AdminBottomNavTab.ORDERS -> navController.navigate(Routes.ADMIN_INVOICE_LIST)
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
        AdminBottomNavTab.SETTINGS -> Unit
        else -> println("Admin Tab selected: $tab")
    }
}