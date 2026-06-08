package com.example.shoestoreapp

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
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
import com.example.shoestoreapp.core.utils.JwtUtils
import com.example.shoestoreapp.core.utils.SignalRManager
import com.example.shoestoreapp.core.utils.TokenManager
import com.example.shoestoreapp.features.admin.addproduct.data.repositories.MasterDataRepository
import com.example.shoestoreapp.features.admin.addproduct.ui.screens.AdminProductCrudScreen
import com.example.shoestoreapp.features.admin.addproduct.viewmodel.AdminAddProductViewModel
import com.example.shoestoreapp.features.admin.addproduct.viewmodel.FetchMasterDataViewModel
import com.example.shoestoreapp.features.admin.ai_assistant.ui.AiStrategyScreen
import com.example.shoestoreapp.features.admin.ai_assistant.viewmodel.AiStrategyViewmodel
import com.example.shoestoreapp.features.admin.analytics.data.AnalyticsRepository
import com.example.shoestoreapp.features.admin.analytics.ui.AdminAnalyticsScreen
import com.example.shoestoreapp.features.admin.analytics.viewmodel.AdminAnalyticsViewModel
import com.example.shoestoreapp.features.admin.invoice.data.AdminInvoiceRepository
import com.example.shoestoreapp.features.admin.invoice.ui.AdminInvoiceScreen
import com.example.shoestoreapp.features.admin.invoice.viewmodel.AdminInvoiceViewmodel
import com.example.shoestoreapp.features.admin.product.ui.components.AdminBottomNavTab
import com.example.shoestoreapp.features.admin.product.ui.screens.AdminProductListScreen
import com.example.shoestoreapp.features.admin.product.viewmodel.AdminProductListViewModel
import com.example.shoestoreapp.features.admin.profile.ui.AdminProfileScreen
import com.example.shoestoreapp.features.admin.voucher.ui.screen.VoucherManagementScreen
import com.example.shoestoreapp.features.agent_intelligent.data.repository.AiChatRepository
import com.example.shoestoreapp.features.agent_intelligent.product_assistant.ui.AiProductScreen
import com.example.shoestoreapp.features.agent_intelligent.product_assistant.viewmodel.AiProductViewmodel
import com.example.shoestoreapp.features.auth.presentation.reset_password.create_new_password.CreateNewPasswordScreen
import com.example.shoestoreapp.features.auth.presentation.reset_password.forgot_password.ForgotPasswordScreen
import com.example.shoestoreapp.features.auth.presentation.sign_in.LoginScreenContent
import com.example.shoestoreapp.features.auth.presentation.sign_up.RegisterScreenContent
import com.example.shoestoreapp.features.auth.presentation.welcome.WelcomeScreen
import com.example.shoestoreapp.features.invoice.model.InvoiceStatus
import com.example.shoestoreapp.features.user.cart.ui.screens.CartScreen
import com.example.shoestoreapp.features.user.cart.viewmodel.CartViewModel
import com.example.shoestoreapp.features.user.checkout.ui.screens.CheckoutScreen
import com.example.shoestoreapp.features.user.checkout.ui.screens.CheckoutScreenActions
import com.example.shoestoreapp.features.user.checkout.ui.screens.PaymentQRScreen
import com.example.shoestoreapp.features.user.invoice.data.UserInvoiceRepository
import com.example.shoestoreapp.features.user.invoice.ui.UserInvoiceScreen
import com.example.shoestoreapp.features.user.invoice.viewmodel.UserInvoiceViewModel
import com.example.shoestoreapp.features.user.product.ui.components.BottomNavTab
import com.example.shoestoreapp.features.user.product.ui.product_detail.ProductDetailScreen
import com.example.shoestoreapp.features.user.product.ui.product_list.ProductListScreen
import com.example.shoestoreapp.features.user.product.viewmodel.ProductDetailViewModel
import com.example.shoestoreapp.features.user.product.viewmodel.ProductListViewModel
import com.example.shoestoreapp.features.user.profile.ui.screens.ChangePasswordScreen
import com.example.shoestoreapp.features.user.profile.ui.screens.CreateAddressScreen
import com.example.shoestoreapp.features.user.profile.ui.screens.EditProfileScreen
import com.example.shoestoreapp.features.user.profile.ui.screens.ManageAddressScreen
import com.example.shoestoreapp.features.user.profile.ui.screens.UserProfileScreen
import com.example.shoestoreapp.features.user.voucher.ui.screens.CollectVoucherScreen
import com.example.shoestoreapp.features.user.voucher.ui.screens.MyVoucherScreen
import com.facebook.login.LoginManager
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
    const val PAYMENT_QR = "payment_qr"
    const val USER_INVOICE_LIST_BASE = "user_invoice_list"
    const val USER_INVOICE_LIST = "$USER_INVOICE_LIST_BASE?status={status}"
    const val USER_PROFILE = "user_profile"
    const val USER_EDIT_PROFILE = "user_edit_profile"
    const val USER_CHANGE_PASSWORD = "user_change_password"
    const val USER_COLLECT_VOUCHERS = "user_collect_vouchers"
    const val USER_MANAGE_ADDRESS_BASE = "user_manage_address"
    const val USER_MANAGE_ADDRESS = "$USER_MANAGE_ADDRESS_BASE?isSelectionMode={isSelectionMode}"
    const val USER_CREATE_ADDRESS_BASE = "user_create_address"
    const val USER_CREATE_ADDRESS = "$USER_CREATE_ADDRESS_BASE?addressId={addressId}"
    const val ADMIN_PRODUCT_LIST = "admin_product_list"
    const val ADMIN_CRUD = "admin_crud"
    const val ADMIN_EDIT_PRODUCT = "admin_edit_product/{productId}"
    const val ADMIN_INVOICE_LIST = "admin_invoice_list"
    const val ADMIN_ANALYTICS = "admin_analytics"
    const val ADMIN_VOUCHER_MANAGEMENT = "admin_voucher_management"
    const val ADMIN_PROFILE = "admin_profile"
    const val ADMIN_EDIT_PROFILE = "admin_edit_profile"
    const val ADMIN_CHANGE_PASSWORD = "admin_change_password"

    const val USER_MY_VOUCHERS =
        "user_my_vouchers?isSelectionMode={isSelectionMode}&cartTotal={cartTotal}"

    const val ADMIN_AI_ASSISTANT_BASE = "admin_ai_assistant"
    const val ADMIN_AI_ASSISTANT =
        "$ADMIN_AI_ASSISTANT_BASE?isGeneratingCampaign={isGeneratingCampaign}"
    const val ADMIN_AI_PRODUCT = "admin_ai_product"
    const val USER_AI_ASSISTANT_BASE = "user_ai_assistant"
    const val USER_AI_ASSISTANT = "$USER_AI_ASSISTANT_BASE?isGenerateProduct={isGeneratingProduct}"

    fun userManageAddress(isSelectionMode: Boolean = false): String {
        return "$USER_MANAGE_ADDRESS_BASE?isSelectionMode=$isSelectionMode"
    }

    fun userCreateAddress(addressId: String? = null): String {
        return if (addressId != null) {
            "$USER_CREATE_ADDRESS_BASE?addressId=$addressId"
        } else {
            USER_CREATE_ADDRESS_BASE
        }
    }

    fun userMyVouchers(isSelectionMode: Boolean = false, cartTotal: Double? = null): String {
        return if (cartTotal != null) {
            "user_my_vouchers?isSelectionMode=$isSelectionMode&cartTotal=$cartTotal"
        } else {
            "user_my_vouchers?isSelectionMode=$isSelectionMode"
        }
    }

    fun createNewPassword(email: String, otp: String): String = "create_new_password/$email/$otp"
    fun adminEditProduct(productId: String): String = "admin_edit_product/$productId"

    fun userInvoiceList(status: InvoiceStatus? = null): String {
        return if (status == null) USER_INVOICE_LIST_BASE else "$USER_INVOICE_LIST_BASE?status=${status.name}"
    }

    fun adminAiAssistant(isGeneratingCampaign: Boolean = false): String {
        return "$ADMIN_AI_ASSISTANT_BASE?isGeneratingCampaign=$isGeneratingCampaign"
    }

    fun adminAiProduct(): String = ADMIN_AI_PRODUCT

    fun userAiAssistant(isGeneratingProduct: Boolean = false): String {
        return "$USER_AI_ASSISTANT_BASE?isGenerateProduct=$isGeneratingProduct"
    }
}

@Suppress("DEPRECATION")
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        RetrofitInstance.init(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
//        try {
//            val info = packageManager.getPackageInfo(
//                packageName,
//                PackageManager.GET_SIGNATURES
//            )
//            for (signature in info.signatures!!) {
//                val md = MessageDigest.getInstance("SHA")
//                md.update(signature.toByteArray())
//                Log.d("MY_KEY_HASH", Base64.encodeToString(md.digest(), Base64.DEFAULT))
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
        setContent {
            MaterialTheme {
                AppNavHost()
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val token by tokenManager.getToken.collectAsState(initial = "LOADING")

    LaunchedEffect(token) {
        if (token == "LOADING") return@LaunchedEffect
        if (token.isNullOrBlank()) {
            val currentRoute = navController.currentDestination?.route
            if (currentRoute != Routes.SIGN_IN && currentRoute != Routes.WELCOME) {
                navController.navigateAfterLogout()
            }
            return@LaunchedEffect
        }
        if (JwtUtils.isTokenExpired(token)) {
            tokenManager.clearAuthInfo()
            navController.navigateAfterLogout()
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.WELCOME
    ) {
        authGraph(navController, tokenManager)
        userGraph(navController, tokenManager)
        adminGraph(navController, tokenManager)
    }
}

private fun NavGraphBuilder.authGraph(
    navController: NavHostController,
    tokenManager: TokenManager
) {
    composable(Routes.WELCOME) {
        val token by tokenManager.getToken.collectAsState(initial = "LOADING")
        val role by tokenManager.getRole.collectAsState(initial = "")

        LaunchedEffect(token, role) {
            if (token == "LOADING") return@LaunchedEffect

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

@RequiresApi(Build.VERSION_CODES.O)
private fun NavGraphBuilder.userGraph(
    navController: NavHostController,
    tokenManager: TokenManager
) {
    composable(Routes.PRODUCT_LIST) {
        ProductListScreen(
            viewModel = remember { ProductListViewModel() },
            onNavigateToDetail = { productGuid, colorName ->
                val route = Routes.PRODUCT_DETAIL
                    .replace("{productGuid}", productGuid)
                    .replace("{colorName}", colorName)
                navController.navigate(route)
            },
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
            actions = CheckoutScreenActions(
                onBackClick = { navController.popBackStack() },
                onShoppingBagClick = {
                    navController.navigateAndPopTo(
                        Routes.CART,
                        Routes.CHECKOUT
                    )
                },
                onNavigateToVoucherScreen = { cartTotal ->
                    navController.navigate(
                        Routes.userMyVouchers(
                            isSelectionMode = true,
                            cartTotal = cartTotal
                        )
                    )
                },
                onNavigateToQRScreen = { invoice ->
                    navController.currentBackStackEntry?.savedStateHandle?.apply {
                        set("orderCode", invoice.orderCode)
                        set("finalPrice", invoice.finalPrice ?: 0.0)
                        set("bankCode", invoice.shopBankCode ?: "")
                        set("bankAccount", invoice.shopBankAccount ?: "")
                        set("accountName", invoice.shopAccountName ?: "")
                    }
                    navController.navigate(Routes.PAYMENT_QR)
                },
                onEditAddressClick = {
                    navController.navigate("user_manage_address?isSelectionMode=true")
                },
                onCompletePurchaseClick = {
                    navController.navigateAndPopTo(Routes.PRODUCT_LIST, Routes.CHECKOUT)
                }
            )
        )
    }

    composable(Routes.PAYMENT_QR) {
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
            onBackClick = {
                navController.navigate(Routes.PRODUCT_LIST) {
                    popUpTo(Routes.PRODUCT_LIST) { inclusive = true }
                }
            },
            onBackToHomeClick = {
                navController.navigate(Routes.PRODUCT_LIST) {
                    popUpTo(Routes.PRODUCT_LIST) { inclusive = true }
                }
            }
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
        UserProfileScreen(
            onTabSelected = { tab -> handleUserProfileTabSelection(tab, navController) },
            onEditProfileClick = { navController.navigate(Routes.USER_EDIT_PROFILE) },
            onChangePasswordClick = { navController.navigate(Routes.USER_CHANGE_PASSWORD) },
            onManageAddressClick = { navController.navigate(Routes.userManageAddress(isSelectionMode = false)) },
            onMyVouchersClick = {
                navController.navigate(
                    Routes.userMyVouchers(
                        isSelectionMode = false,
                        cartTotal = null
                    )
                )
            },
            onMyOrdersClick = {
                navController.navigate(Routes.userInvoiceList())
            },
            onLogoutClick = {
                scope.launch {
                    tokenManager.clearAuthInfo()
                    LoginManager.getInstance().logOut()
                    navController.navigateAfterLogout()
                }
            },
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
        route = Routes.USER_MANAGE_ADDRESS,
        arguments = listOf(
            navArgument("isSelectionMode") {
                type = NavType.BoolType
                defaultValue = false
            }
        )
    ) { backStackEntry ->
        val isSelectionMode = backStackEntry.arguments?.getBoolean("isSelectionMode") ?: false

        ManageAddressScreen(
            isSelectionMode = isSelectionMode,
            onBackClick = { navController.popBackStack() },
            onAddAddressClick = { navController.navigate(Routes.userCreateAddress(null)) },
            onEditAddressClick = { addressId ->
                navController.navigate(
                    Routes.userCreateAddress(
                        addressId
                    )
                )
            },
            onAddressSelected = { selectedId ->
                navController.previousBackStackEntry?.savedStateHandle?.set(
                    "selected_address_id",
                    selectedId
                )
                navController.popBackStack()
            }
        )
    }

    composable(
        route = Routes.USER_CREATE_ADDRESS,
        arguments = listOf(
            navArgument("addressId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) { backStackEntry ->
        val addressId = backStackEntry.arguments?.getString("addressId")

        CreateAddressScreen(
            addressId = addressId,
            onBackClick = {
                navController.popBackStack()
            }
        )
    }

    composable(
        route = Routes.USER_MY_VOUCHERS,
        arguments = listOf(
            navArgument("isSelectionMode") {
                type = NavType.BoolType
                defaultValue = false
            },
            navArgument("cartTotal") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) { backStackEntry ->
        val isSelectionMode = backStackEntry.arguments?.getBoolean("isSelectionMode") ?: false
        val cartTotalString = backStackEntry.arguments?.getString("cartTotal")
        val cartTotal = cartTotalString?.toDoubleOrNull()

        MyVoucherScreen(
            isSelectionMode = isSelectionMode,
            cartTotal = cartTotal,
            onBackClick = { navController.popBackStack() },
            onApplyVoucher = { voucherUiModel ->
                val voucherJson = com.google.gson.Gson().toJson(voucherUiModel)
                navController.previousBackStackEntry?.savedStateHandle?.set(
                    "selected_voucher_json",
                    voucherJson
                )
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

    composable(
        route = "${Routes.USER_AI_ASSISTANT_BASE}?isGenerateProduct={isGeneratingProduct}",
        arguments = listOf(
            navArgument("isGeneratingProduct") {
                type = NavType.BoolType
                defaultValue = false
            }
        )
    ) { backStackEntry ->
        val signalRManager = remember { SignalRManager(tokenManager) }
        val isGeneratingProduct =
            backStackEntry.arguments?.getBoolean("isGeneratingProduct") ?: false
        val initialPrompt = if (isGeneratingProduct) {
            """Generate product recommendation based on user's purchase history and preferences
            """.trimIndent()
        } else {
            null
        }
        val aiProductViewmodel = remember {
            AiProductViewmodel(
                repository = AiChatRepository(
                    sessionApi = RetrofitInstance.chatSessionApi,
                    okHttpClient = RetrofitInstance.okHttpClient
                ),
                signalRManager = signalRManager
            )
        }
        AiProductScreen(
            viewModel = aiProductViewmodel,
            onBackClick = { navController.popBackStack() },
            onTabSelected = { tab -> handleUserAiTabSelection(tab, navController) }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun NavGraphBuilder.adminGraph(
    navController: NavHostController,
    tokenManager: TokenManager
) {
    composable(Routes.ADMIN_PRODUCT_LIST) {
        AdminProductListScreen(
            viewModel = remember { AdminProductListViewModel() },
            onAiAssistantClick = {
                navController.navigate(Routes.adminAiProduct())
            },
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
            viewModel = remember {
                AdminInvoiceViewmodel(
                    repository = AdminInvoiceRepository(RetrofitInstance.invoiceApi)
                )
            },
            onTabSelected = { tab -> handleAdminInvoiceTabSelection(tab, navController) }
        )
    }

    composable(Routes.ADMIN_ANALYTICS) {
        AdminAnalyticsScreen(
            viewModel = remember {
                AdminAnalyticsViewModel(
                    repository = AnalyticsRepository(RetrofitInstance.analyticsApi)
                )
            },
            onGenerateCampaignClick = {
                navController.navigate(Routes.adminAiAssistant(isGeneratingCampaign = true))
            },
            onAiClick = { navController.navigate(Routes.adminAiAssistant(isGeneratingCampaign = false)) },
            onTabSelected = { tab -> handleAdminAnalyticsTabSelection(tab, navController) }
        )
    }

    // Tích hợp AI từ GitHub
    composable(
        route = "${Routes.ADMIN_AI_ASSISTANT_BASE}?isGeneratingCampaign={isGeneratingCampaign}",
        arguments = listOf(
            navArgument("isGeneratingCampaign") {
                type = NavType.BoolType
                defaultValue = false
            }
        )
    ) { backStackEntry ->
        // Get context and create pile to connect SignalR
        val context = LocalContext.current
        val tokenManager = remember { TokenManager(context) }
        val signalRManager = remember { SignalRManager(tokenManager) }
        val isGeneratingCampaign =
            backStackEntry.arguments?.getBoolean("isGeneratingCampaign") ?: false
        val initialPrompt = if (isGeneratingCampaign) {
            """
            Generate campaign to improve shop's revenue with the data based on shop's statistics
            """.trimIndent()
        } else {
            null
        }

        val aiStrategyViewmodel = remember {
            AiStrategyViewmodel(
                repository = AiChatRepository(
                    sessionApi = RetrofitInstance.chatSessionApi, // Reuse authApi for session management
                    okHttpClient = RetrofitInstance.okHttpClient // Use the same OkHttpClient for streaming
                ),
                signalRManager = signalRManager
            )
        }
        AiStrategyScreen(
            viewModel = aiStrategyViewmodel,
            initialPrompt = initialPrompt,
            onBackClick = { navController.popBackStack() }
        )
    }

    composable(Routes.ADMIN_AI_PRODUCT) {
        val context = LocalContext.current
        val tokenManager = remember { TokenManager(context) }
        val signalRManager = remember { SignalRManager(tokenManager) }
        val aiProductViewmodel = remember {
            AiProductViewmodel(
                repository = AiChatRepository(
                    sessionApi = RetrofitInstance.chatSessionApi,
                    okHttpClient = RetrofitInstance.okHttpClient
                ),
                signalRManager = signalRManager,
                enableAdminActions = true
            )
        }
        AiProductScreen(
            viewModel = aiProductViewmodel,
            onBackClick = { navController.popBackStack() },
            showAdminPanels = true,
            title = "Admin Product Assistant",
            aiRoleName = "AI ADMIN",
            userRoleName = "ADMIN"
        )
    }

    composable(Routes.ADMIN_PROFILE) {
        val scope = rememberCoroutineScope()

        AdminProfileScreen(
            onTabSelected = { tab -> handleAdminProfileTabSelection(tab, navController) },
            onEditProfileClick = { navController.navigate(Routes.ADMIN_EDIT_PROFILE) },
            onChangePasswordClick = { navController.navigate(Routes.ADMIN_CHANGE_PASSWORD) },
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

    composable(Routes.ADMIN_EDIT_PROFILE) {
        EditProfileScreen(
            onBackClick = { navController.popBackStack() }
        )
    }

    composable(Routes.ADMIN_CHANGE_PASSWORD) {
        ChangePasswordScreen(
            onBackClick = { navController.popBackStack() }
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
        BottomNavTab.AI -> navController.navigate(Routes.userAiAssistant())
        else -> Unit
    }
}

private fun handleUserInvoiceTabSelection(tab: BottomNavTab, navController: NavHostController) {
    when (tab) {
        BottomNavTab.SHOP -> {
            navController.navigateAndPopTo(Routes.PRODUCT_LIST, Routes.USER_INVOICE_LIST_BASE)
        }

        BottomNavTab.PROFILE -> {
            navController.navigateAndPopTo(Routes.USER_PROFILE, Routes.USER_INVOICE_LIST_BASE)
        }

        BottomNavTab.VOUCHER -> {
            navController.navigateAndPopTo(
                Routes.USER_COLLECT_VOUCHERS,
                Routes.USER_INVOICE_LIST_BASE
            )
        }

        BottomNavTab.BAG -> {
            navController.navigateAndPopTo(Routes.CART, Routes.USER_INVOICE_LIST_BASE)
        }
        BottomNavTab.AI -> {
            navController.navigateAndPopTo(Routes.userAiAssistant(), Routes.USER_INVOICE_LIST_BASE)
        }

        else -> println("User Tab selected: $tab")
    }
}

private fun handleUserBagTabSelection(tab: BottomNavTab, navController: NavHostController) {
    when (tab) {
        BottomNavTab.SHOP -> {
            navController.navigateAndPopTo(Routes.PRODUCT_LIST, Routes.CART)
        }

        BottomNavTab.PROFILE -> {
            navController.navigateAndPopTo(Routes.USER_PROFILE, Routes.CART)
        }

        BottomNavTab.VOUCHER -> {
            navController.navigateAndPopTo(Routes.USER_COLLECT_VOUCHERS, Routes.CART)
        }

        BottomNavTab.BAG -> Unit
        BottomNavTab.AI -> {
            navController.navigateAndPopTo(Routes.userAiAssistant(), Routes.CART)
        }

        else -> println("User Tab selected: $tab")
    }
}

private fun handleUserProfileTabSelection(tab: BottomNavTab, navController: NavHostController) {
    when (tab) {
        BottomNavTab.SHOP -> {
            navController.navigateAndPopTo(Routes.PRODUCT_LIST, Routes.USER_PROFILE)
        }

        BottomNavTab.BAG -> {
            navController.navigateAndPopTo(Routes.CART, Routes.USER_PROFILE)
        }

        BottomNavTab.VOUCHER -> {
            navController.navigateAndPopTo(Routes.USER_COLLECT_VOUCHERS, Routes.USER_PROFILE)
        }

        BottomNavTab.PROFILE -> Unit
        BottomNavTab.AI -> {
            navController.navigateAndPopTo(Routes.userAiAssistant(), Routes.USER_PROFILE)
        }

        else -> println("User Tab selected: $tab")
    }
}

private fun handleUserVoucherTabSelected(tab: BottomNavTab, navController: NavHostController) {
    when (tab) {
        BottomNavTab.SHOP -> {
            navController.navigateAndPopTo(Routes.PRODUCT_LIST, Routes.CART)
        }

        BottomNavTab.BAG -> {
            navController.navigateAndPopTo(Routes.CART, Routes.CART)
        }

        BottomNavTab.PROFILE -> {
            navController.navigateAndPopTo(Routes.USER_PROFILE, Routes.CART)
        }

        BottomNavTab.VOUCHER -> Unit
        BottomNavTab.AI -> {
            navController.navigateAndPopTo(Routes.userAiAssistant(), Routes.USER_COLLECT_VOUCHERS)
        }

        else -> println("User Tab selected: $tab")
    }
}

private fun handleUserAiTabSelection(tab: BottomNavTab, navController: NavHostController) {
    when (tab) {
        BottomNavTab.SHOP -> {
            navController.navigateAndPopTo(Routes.PRODUCT_LIST, Routes.USER_AI_ASSISTANT)
        }

        BottomNavTab.BAG -> {
            navController.navigateAndPopTo(Routes.CART, Routes.USER_AI_ASSISTANT)
        }

        BottomNavTab.VOUCHER -> {
            navController.navigateAndPopTo(Routes.USER_COLLECT_VOUCHERS, Routes.USER_AI_ASSISTANT)
        }

        BottomNavTab.PROFILE -> {
            navController.navigateAndPopTo(Routes.USER_PROFILE, Routes.USER_AI_ASSISTANT)
        }

        BottomNavTab.AI -> Unit
    }
}

private fun handleAdminProductTabSelection(
    tab: AdminBottomNavTab,
    navController: NavHostController
) {
    when (tab) {
        AdminBottomNavTab.ADMIN -> Unit
        AdminBottomNavTab.ORDERS -> navController.navigate(Routes.ADMIN_INVOICE_LIST)
        AdminBottomNavTab.VOUCHERS -> navController.navigate(Routes.ADMIN_VOUCHER_MANAGEMENT)
        AdminBottomNavTab.ANALYTICS -> navController.navigate(Routes.ADMIN_ANALYTICS)
        AdminBottomNavTab.PROFILE -> navController.navigate(Routes.ADMIN_PROFILE)
    }
}

private fun handleAdminInvoiceTabSelection(
    tab: AdminBottomNavTab,
    navController: NavHostController
) {
    when (tab) {
        AdminBottomNavTab.ADMIN -> {
            navController.navigateAndPopTo(Routes.ADMIN_PRODUCT_LIST, Routes.ADMIN_INVOICE_LIST)
        }

        AdminBottomNavTab.ORDERS -> Unit
        AdminBottomNavTab.VOUCHERS -> {
            navController.navigateAndPopTo(
                Routes.ADMIN_VOUCHER_MANAGEMENT,
                Routes.ADMIN_INVOICE_LIST
            )
        }

        AdminBottomNavTab.ANALYTICS -> {
            navController.navigateAndPopTo(Routes.ADMIN_ANALYTICS, Routes.ADMIN_INVOICE_LIST)
        }

        AdminBottomNavTab.PROFILE -> {
            navController.navigateAndPopTo(Routes.ADMIN_PROFILE, Routes.ADMIN_INVOICE_LIST)
        }
    }
}

private fun handleAdminProfileTabSelection(
    tab: AdminBottomNavTab,
    navController: NavHostController
) {
    when (tab) {
        AdminBottomNavTab.ADMIN -> {
            navController.navigateAndPopTo(Routes.ADMIN_PRODUCT_LIST, Routes.ADMIN_PROFILE)
        }

        AdminBottomNavTab.ORDERS -> {
            navController.navigateAndPopTo(Routes.ADMIN_INVOICE_LIST, Routes.ADMIN_PROFILE)
        }

        AdminBottomNavTab.VOUCHERS -> {
            navController.navigateAndPopTo(Routes.ADMIN_VOUCHER_MANAGEMENT, Routes.ADMIN_PROFILE)
        }

        AdminBottomNavTab.ANALYTICS -> {
            navController.navigateAndPopTo(Routes.ADMIN_ANALYTICS, Routes.ADMIN_PROFILE)
        }

        AdminBottomNavTab.PROFILE -> Unit
    }
}

private fun handleAdminAnalyticsTabSelection(
    tab: AdminBottomNavTab,
    navController: NavHostController
) {
    when (tab) {
        AdminBottomNavTab.ADMIN -> navController.navigateAndPopTo(
            Routes.ADMIN_PRODUCT_LIST,
            Routes.ADMIN_ANALYTICS
        )

        AdminBottomNavTab.ORDERS -> navController.navigateAndPopTo(
            Routes.ADMIN_INVOICE_LIST,
            Routes.ADMIN_ANALYTICS
        )

        AdminBottomNavTab.VOUCHERS -> navController.navigateAndPopTo(
            Routes.ADMIN_VOUCHER_MANAGEMENT,
            Routes.ADMIN_ANALYTICS
        )

        AdminBottomNavTab.PROFILE -> navController.navigateAndPopTo(
            Routes.ADMIN_PROFILE,
            Routes.ADMIN_ANALYTICS
        )

        else -> Unit
    }
}

private fun handleAdminVoucherTabSelection(
    tab: AdminBottomNavTab,
    navController: NavHostController
) {
    when (tab) {
        AdminBottomNavTab.ADMIN -> {
            navController.navigateAndPopTo(
                Routes.ADMIN_PRODUCT_LIST,
                Routes.ADMIN_VOUCHER_MANAGEMENT
            )
        }

        AdminBottomNavTab.ORDERS -> {
            navController.navigateAndPopTo(
                Routes.ADMIN_INVOICE_LIST,
                Routes.ADMIN_VOUCHER_MANAGEMENT
            )
        }

        AdminBottomNavTab.VOUCHERS -> Unit
        AdminBottomNavTab.ANALYTICS -> {
            navController.navigateAndPopTo(Routes.ADMIN_ANALYTICS, Routes.ADMIN_VOUCHER_MANAGEMENT)
        }

        AdminBottomNavTab.PROFILE -> {
            navController.navigateAndPopTo(Routes.ADMIN_PROFILE, Routes.ADMIN_VOUCHER_MANAGEMENT)
        }
    }
}
