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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.shoestoreapp.core.networks.RetrofitInstance
import com.example.shoestoreapp.features.user.product.ui.product_detail.ProductDetailScreen
import com.example.shoestoreapp.features.user.product.ui.product_list.ProductListScreen
import com.example.shoestoreapp.features.user.cart.ui.screens.CartScreen
import com.example.shoestoreapp.features.user.cart.viewmodel.CartViewModel
import com.example.shoestoreapp.features.user.checkout.ui.screens.CheckoutScreen
import com.example.shoestoreapp.features.user.checkout.ui.screens.PaymentQRScreen
import com.example.shoestoreapp.features.user.voucher.ui.screens.CollectVoucherScreen
import com.example.shoestoreapp.features.admin.invoice.ui.AdminInvoiceScreen
import com.example.shoestoreapp.features.admin.product.ui.components.AdminBottomNavTab
import com.example.shoestoreapp.features.user.product.ui.components.BottomNavTab
import com.example.shoestoreapp.features.user.product.viewmodel.ProductDetailViewModel
import com.example.shoestoreapp.features.user.product.viewmodel.ProductListViewModel
import com.example.shoestoreapp.features.user.invoice.ui.UserInvoiceScreen
import com.example.shoestoreapp.features.user.profile.ui.screens.ChangePasswordScreen
import com.example.shoestoreapp.features.user.profile.ui.screens.EditProfileScreen
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
import com.example.shoestoreapp.features.user.voucher.ui.screens.MyVoucherScreen
import kotlinx.coroutines.launch

private object Routes {
    const val WELCOME = "welcome"
    const val SIGN_IN = "sign_in"
    const val SIGN_UP = "sign_up"
    const val FORGOT_PASSWORD = "forgot_password"
    const val CREATE_NEW_PASSWORD = "create_new_password/{email}/{otp}"
    const val PRODUCT_LIST = "product_list"
    const val PRODUCT_DETAIL = "product_detail/{productGuid}?colorName={colorName}"
    const val CART = "cart"
    const val CHECKOUT = "checkout"
    const val PAYMENT_QR = "payment_qr" // ROUTE MOI CHO MAN HINH QR
    const val USER_INVOICE_LIST = "user_invoice_list"
    const val USER_PROFILE = "user_profile"
    const val USER_EDIT_PROFILE = "user_edit_profile"
    const val USER_CHANGE_PASSWORD = "user_change_password"
    const val USER_COLLECT_VOUCHERS = "user_collect_vouchers"
    const val ADMIN_PRODUCT_LIST = "admin_product_list"
    const val ADMIN_CRUD = "admin_crud"
    const val ADMIN_EDIT_PRODUCT = "admin_edit_product/{productId}"
    const val ADMIN_INVOICE_LIST = "admin_invoice_list"
    const val ADMIN_SETTINGS = "admin_settings"
    const val ADMIN_VOUCHER_MANAGEMENT = "admin_voucher_management"

    const val USER_MY_VOUCHERS = "user_my_vouchers?isSelectionMode={isSelectionMode}"
    fun userMyVouchers(isSelectionMode: Boolean = false): String = "user_my_vouchers?isSelectionMode=$isSelectionMode"
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
            onNavigateToDetail = { productGuid, colorName ->
                println("onNavigateToDetail called - productGuid: $productGuid")
                val route = Routes.PRODUCT_DETAIL
                    .replace("{productGuid}", productGuid)
                    .replace("{colorName}", colorName)
                navController.navigate(route)
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
            onTabSelected = { tab -> handleUserBagTabSelection(tab, navController) }
        )
    }

    composable(Routes.CHECKOUT) {
        CheckoutScreen(
            navController = navController,
            onBackClick = { navController.popBackStack() },
            onShoppingBagClick = { navController.navigateAndPopTo(Routes.CART, Routes.CHECKOUT) },
            onNavigateToVoucherScreen = {
                // Mo man hinh voucher o che do chon
                navController.navigate(Routes.userMyVouchers(isSelectionMode = true))
            },
            onNavigateToQRScreen = { invoice ->
                // Nhet du lieu vao savedStateHandle roi chuyen trang
                navController.currentBackStackEntry?.savedStateHandle?.apply {
                    set("orderCode", invoice.orderCode)
                    set("finalPrice", invoice.finalPrice ?: 0.0)
                    set("bankCode", invoice.shopBankCode ?: "")
                    set("bankAccount", invoice.shopBankAccount ?: "")
                    set("accountName", invoice.shopAccountName ?: "")
                }
                navController.navigate(Routes.PAYMENT_QR)
            },
            onCompletePurchaseClick = {
                navController.navigateAndPopTo(Routes.PRODUCT_LIST, Routes.CHECKOUT)
            }
        )
    }

    composable(Routes.PAYMENT_QR) {
        // Lay du lieu tu man hinh Checkout truyen sang
        val arguments = navController.previousBackStackEntry?.savedStateHandle
        val orderCode = arguments?.get<String>("orderCode") ?: ""
        val finalPrice = arguments?.get<Double>("finalPrice") ?: 0.0
        val bankCode = arguments?.get<String>("bankCode") ?: ""
        val bankAccount = arguments?.get<String>("bankAccount") ?: ""
        val accountName = arguments?.get<String>("accountName") ?: ""

        PaymentQRScreen(
            orderCode = orderCode,
            finalPrice = finalPrice,
            bankCode = bankCode,
            bankAccount = bankAccount,
            accountName = accountName,
            onBackToHomeClick = {
                // Xoa toan bo backstack tro ve trang chu
                navController.navigate(Routes.PRODUCT_LIST) {
                    popUpTo(Routes.PRODUCT_LIST) { inclusive = true }
                }
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
            onEditProfileClick = { navController.navigate(Routes.USER_EDIT_PROFILE) },
            onChangePasswordClick = { navController.navigate(Routes.USER_CHANGE_PASSWORD) },
            onMyVouchersClick = {navController.navigate(Routes.userMyVouchers(isSelectionMode = false))},
            onLogoutClick = {
                scope.launch {
                    tokenManager.clearAuthInfo()
                    navController.navigateAfterLogout()
                }
            }
        )
    }

    composable(Routes.USER_COLLECT_VOUCHERS) {
        CollectVoucherScreen(
            onTabSelected = { tab -> handleUserVoucherTabSelected(tab, navController) }
        )
    }

    composable(Routes.USER_EDIT_PROFILE) {
        EditProfileScreen(
            onBackClick = { navController.popBackStack() }
        )
    }

    composable(Routes.USER_CHANGE_PASSWORD) {
        ChangePasswordScreen(
            onBackClick = { navController.popBackStack() }
        )
    }

    composable(
        route = Routes.USER_MY_VOUCHERS,
        arguments = listOf(
            navArgument("isSelectionMode") {
                type = NavType.BoolType
                defaultValue = false
            }
        )
    ) { backStackEntry ->
        val isSelectionMode = backStackEntry.arguments?.getBoolean("isSelectionMode") ?: false

        MyVoucherScreen(
            isSelectionMode = isSelectionMode,
            onBackClick = { navController.popBackStack() },
            onApplyVoucher = { voucherUiModel ->
                val voucherJson = com.google.gson.Gson().toJson(voucherUiModel)

                navController.previousBackStackEntry?.savedStateHandle?.set("selected_voucher_json", voucherJson)
                navController.popBackStack()
            },
            onShopNowClick = {
                navController.navigateAndPopTo(Routes.PRODUCT_LIST, Routes.USER_MY_VOUCHERS)
            }
        )
    }

    composable(
        route = Routes.PRODUCT_DETAIL,
        arguments = listOf(
            navArgument("colorName") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) { backStackEntry ->
        val productGuid = backStackEntry.arguments?.getString("productGuid") ?: "unknown"
        val passedColorName = backStackEntry.arguments?.getString("colorName")

        ProductDetailScreen(
            productGuid = productGuid,
            passedColorName = passedColorName,
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
        BottomNavTab.VOUCHER -> navController.navigate(Routes.USER_COLLECT_VOUCHERS)
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
        BottomNavTab.VOUCHER -> {
            navController.navigateAndPopTo(Routes.USER_COLLECT_VOUCHERS, Routes.USER_INVOICE_LIST)
        }
        BottomNavTab.BAG -> Unit
        else -> println("User Tab selected: $tab")
    }
}

private fun handleUserBagTabSelection(tab: BottomNavTab, navController: NavHostController) {
    when (tab) {
        BottomNavTab.HOME, BottomNavTab.SHOP -> {
            navController.navigateAndPopTo(Routes.PRODUCT_LIST, Routes.CART)
        }
        BottomNavTab.PROFILE -> {
            navController.navigateAndPopTo(Routes.USER_PROFILE, Routes.CART)
        }
        BottomNavTab.VOUCHER -> {
            navController.navigateAndPopTo(Routes.USER_COLLECT_VOUCHERS, Routes.CART)
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
        BottomNavTab.VOUCHER -> {
            navController.navigateAndPopTo(Routes.USER_COLLECT_VOUCHERS, Routes.USER_PROFILE)
        }
        BottomNavTab.PROFILE -> Unit
        else -> println("User Tab selected: $tab")
    }
}

private fun handleUserVoucherTabSelected(tab: BottomNavTab, navController: NavHostController) {
    when (tab) {
        BottomNavTab.HOME, BottomNavTab.SHOP -> {
            navController.navigateAndPopTo(Routes.PRODUCT_LIST, Routes.CART)
        }
        BottomNavTab.BAG -> {
            navController.navigateAndPopTo(Routes.CART, Routes.CART)
        }
        BottomNavTab.PROFILE -> {
            navController.navigateAndPopTo(Routes.USER_PROFILE, Routes.CART)
        }
        BottomNavTab.VOUCHER -> Unit
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