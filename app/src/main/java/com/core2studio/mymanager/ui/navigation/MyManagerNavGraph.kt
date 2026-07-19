package com.core2studio.mymanager.ui.navigation

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.core2studio.mymanager.MyManagerViewModelFactory
import com.core2studio.mymanager.ui.screens.auth.AuthViewModel
import com.core2studio.mymanager.ui.screens.auth.EmailVerificationScreen
import com.core2studio.mymanager.ui.screens.auth.LoginScreen
import com.core2studio.mymanager.ui.screens.auth.SignUpScreen
import com.core2studio.mymanager.ui.screens.auth.ForgotPasswordScreen
import com.core2studio.mymanager.ui.screens.categories.CategoriesScreen
import com.core2studio.mymanager.ui.screens.categories.CategoryViewModel
import com.core2studio.mymanager.ui.screens.clients.ClientDetailScreen
import com.core2studio.mymanager.ui.screens.clients.ClientViewModel
import com.core2studio.mymanager.ui.screens.clients.ClientsScreen
import com.core2studio.mymanager.ui.screens.dashboard.DashboardScreen
import com.core2studio.mymanager.ui.screens.dashboard.DashboardViewModel
import com.core2studio.mymanager.ui.screens.products.AddProductScreen
import com.core2studio.mymanager.ui.screens.products.ProductDetailScreen
import com.core2studio.mymanager.ui.screens.products.ProductViewModel
import com.core2studio.mymanager.ui.screens.products.ProductsScreen
import com.core2studio.mymanager.ui.screens.settings.BusinessCardScreen
import com.core2studio.mymanager.ui.screens.settings.BusinessInfoScreen
import com.core2studio.mymanager.ui.screens.settings.SettingsScreen
import com.core2studio.mymanager.ui.screens.settings.SettingsViewModel
import com.core2studio.mymanager.ui.screens.settings.UserProfileScreen
import com.core2studio.mymanager.ui.screens.orders.AddOrderScreen
import com.core2studio.mymanager.ui.screens.orders.OrderDetailScreen
import com.core2studio.mymanager.ui.screens.orders.OrderViewModel
import com.core2studio.mymanager.ui.screens.orders.OrdersScreen
import com.core2studio.mymanager.ui.screens.cart.CartScreen
import com.core2studio.mymanager.ui.screens.cart.CheckoutScreen
import com.core2studio.mymanager.ui.screens.cart.CartViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Dashboard : com.core2studio.mymanager.ui.navigation.BottomNavItem("dashboard", "Dashboard", Icons.Filled.Home)
    data object Catalog : com.core2studio.mymanager.ui.navigation.BottomNavItem("categories", "Catalog", Icons.Filled.Category)
    data object Orders : com.core2studio.mymanager.ui.navigation.BottomNavItem("orders", "Orders", Icons.Filled.Receipt)
    data object Clients : com.core2studio.mymanager.ui.navigation.BottomNavItem("clients", "Clients", Icons.Filled.People)
    data object Settings : com.core2studio.mymanager.ui.navigation.BottomNavItem("settings", "Account", Icons.Filled.ManageAccounts)
}

private val bottomNavItems = listOf(
    com.core2studio.mymanager.ui.navigation.BottomNavItem.Dashboard,
    com.core2studio.mymanager.ui.navigation.BottomNavItem.Catalog,
    com.core2studio.mymanager.ui.navigation.BottomNavItem.Orders,
    com.core2studio.mymanager.ui.navigation.BottomNavItem.Clients,
    com.core2studio.mymanager.ui.navigation.BottomNavItem.Settings
)

private val bottomNavRoutes = setOf("dashboard", "categories", "orders", "clients", "settings")

@Composable
fun MyManagerNavGraph(
    factory: MyManagerViewModelFactory
) {
    val authViewModel: com.core2studio.mymanager.ui.screens.auth.AuthViewModel = viewModel(factory = factory)
    val authState by authViewModel.uiState.collectAsState()

    // Google Sign-In must be at the root so it's available from both auth and main content
    val context = LocalContext.current
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d("MyManager", "Google Sign-In result: resultCode=${result.resultCode}, data=${result.data != null}")
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                Log.d("MyManager", "Google Sign-In idToken=${idToken != null}")
                if (idToken != null) {
                    authViewModel.signInWithGoogle(idToken)
                } else {
                    Log.e("MyManager", "Google Sign-In: idToken is null")
                    authViewModel.setGoogleSignInError("Google sign-in failed: could not get authentication token. Please try again.")
                }
            } catch (e: ApiException) {
                Log.e("MyManager", "Google Sign-In failed with ApiException: ${e.statusCode}", e)
                val message = when (e.statusCode) {
                    12500 -> "Google sign-in was cancelled. Please try again."
                    12501 -> "Google sign-in was cancelled by the user."
                    12502 -> "Sign-in timed out. Please try again."
                    12503 -> "Network error. Please check your connection and try again."
                    else -> "Google sign-in failed (error ${e.statusCode}). Please try again."
                }
                authViewModel.setGoogleSignInError(message)
            } catch (e: Exception) {
                Log.e("MyManager", "Google Sign-In failed", e)
                authViewModel.setGoogleSignInError("Google sign-in failed. Please try again.")
            }
        } else {
            Log.e("MyManager", "Google Sign-In cancelled or failed: resultCode=${result.resultCode}")
            if (result.resultCode != Activity.RESULT_CANCELED) {
                authViewModel.setGoogleSignInError("Google sign-in failed. Please try again.")
            }
        }
        authViewModel.onGoogleSignInHandled()
    }

    LaunchedEffect(authState.isGoogleSignInTriggered) {
        if (authState.isGoogleSignInTriggered) {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(com.core2studio.mymanager.R.string.google_web_client_id))
                .requestEmail()
                .build()
            val googleSignInClient = GoogleSignIn.getClient(context, gso)
            googleSignInClient.signOut().addOnCompleteListener {
                googleSignInLauncher.launch(googleSignInClient.signInIntent)
            }
        }
    }

    // Fully remount the app when auth state changes
    key(authState.isSignedIn) {
        when {
            authState.isSignedIn -> {
                com.core2studio.mymanager.ui.navigation.MainContent(
                    factory = factory,
                    authViewModel = authViewModel
                )
            }
            authState.showBusinessInfoSetup -> {
                com.core2studio.mymanager.ui.screens.auth.BusinessInfoSetupScreen(
                    viewModel = authViewModel
                )
            }
            authState.showVerificationScreen -> {
                com.core2studio.mymanager.ui.screens.auth.EmailVerificationScreen(
                    viewModel = authViewModel
                )
            }
            else -> {
                com.core2studio.mymanager.ui.navigation.AuthContent(authViewModel = authViewModel)
            }
        }
    }
}

@Composable
private fun AuthContent(authViewModel: com.core2studio.mymanager.ui.screens.auth.AuthViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            com.core2studio.mymanager.ui.screens.auth.LoginScreen(
                viewModel = authViewModel,
                onNavigateToSignUp = { navController.navigate("signup") },
                onNavigateToForgotPassword = { email ->
                    navController.navigate("forgot_password/${android.net.Uri.encode(email)}")
                }
            )
        }
        composable("signup") {
            com.core2studio.mymanager.ui.screens.auth.SignUpScreen(
                viewModel = authViewModel,
                onNavigateToLogin = { navController.popBackStack() }
            )
        }
        composable(
            route = "forgot_password/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("email") ?: "",
                "UTF-8"
            )
            com.core2studio.mymanager.ui.screens.auth.ForgotPasswordScreen(
                viewModel = authViewModel,
                initialEmail = email,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun MainContent(
    factory: MyManagerViewModelFactory,
    authViewModel: com.core2studio.mymanager.ui.screens.auth.AuthViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Shared ViewModels scoped to this content tree
    val dashboardViewModel: com.core2studio.mymanager.ui.screens.dashboard.DashboardViewModel = viewModel(factory = factory)
    val categoryViewModel: com.core2studio.mymanager.ui.screens.categories.CategoryViewModel = viewModel(factory = factory)
    val productViewModel: com.core2studio.mymanager.ui.screens.products.ProductViewModel = viewModel(factory = factory)
    val orderViewModel: com.core2studio.mymanager.ui.screens.orders.OrderViewModel = viewModel(factory = factory)
    val clientViewModel: com.core2studio.mymanager.ui.screens.clients.ClientViewModel = viewModel(factory = factory)
    val settingsViewModel: com.core2studio.mymanager.ui.screens.settings.SettingsViewModel = viewModel(factory = factory)
    val cartViewModel: com.core2studio.mymanager.ui.screens.cart.CartViewModel = viewModel(factory = factory)

    LaunchedEffect(Unit) {
        settingsViewModel.loadSettings()
    }

    val settingsUiState by settingsViewModel.uiState.collectAsState()

    LaunchedEffect(settingsUiState.businessName, settingsUiState.businessEmail, settingsUiState.businessPhone, settingsUiState.businessAddress, settingsUiState.website, settingsUiState.gstin) {
        orderViewModel.setBusinessInfo(
            name = settingsUiState.businessName,
            email = settingsUiState.businessEmail,
            phone = settingsUiState.businessPhone,
            address = settingsUiState.businessAddress,
            website = settingsUiState.website,
            gstin = settingsUiState.gstin
        )
    }

    val showBottomBar = currentRoute in com.core2studio.mymanager.ui.navigation.bottomNavRoutes

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    com.core2studio.mymanager.ui.navigation.bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(text = item.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.outlineVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.outlineVariant,
                                indicatorColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("dashboard") {
                val uiState by dashboardViewModel.uiState.collectAsState()
                com.core2studio.mymanager.ui.screens.dashboard.DashboardScreen(
                    onNavigateToOrders = { navController.navigate("add_order") },
                    onNavigateToCategories = {
                        navController.navigate("categories") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToCatalog = {
                        navController.navigate("categories") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    totalRevenue = uiState.totalRevenue,
                    pendingBalance = uiState.pendingBalance,
                    orderCount = uiState.orderCount,
                    completedCount = uiState.completedCount,
                    products = uiState.recentProducts,
                    totalProductCount = uiState.totalProductCount,
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = { dashboardViewModel.refresh() },
                    errorMessage = uiState.errorMessage,
                    onDismissError = { dashboardViewModel.clearError() }
                )
            }

            composable("categories") {
                val uiState by categoryViewModel.uiState.collectAsState()
                val cartUiState by cartViewModel.uiState.collectAsState()
                com.core2studio.mymanager.ui.screens.categories.CategoriesScreen(
                    onCategoryClick = { categoryId -> navController.navigate("categories/$categoryId") },
                    onCartClick = { navController.navigate("cart") },
                    categories = uiState.categories,
                    productCountResolver = { catId -> uiState.productCounts[catId] ?: 0 },
                    cartCount = cartUiState.cartCount,
                    onAddCategory = { name, desc -> categoryViewModel.addCategory(name, desc) },
                    onUpdateCategory = { category -> categoryViewModel.updateCategory(category) },
                    onDeleteCategory = { category -> categoryViewModel.deleteCategory(category) }
                )
            }

            composable(
                route = "categories/{categoryId}",
                arguments = listOf(navArgument("categoryId") { type = NavType.StringType })
            ) { backStackEntry ->
                val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
                val uiState by productViewModel.uiState.collectAsState()
                val shareUiState by productViewModel.shareUiState.collectAsState()
                val context = androidx.compose.ui.platform.LocalContext.current
                androidx.compose.runtime.LaunchedEffect(categoryId) {
                    productViewModel.loadProducts(categoryId)
                }
                androidx.compose.runtime.LaunchedEffect(shareUiState.shareUri) {
                    shareUiState.shareUri?.let { uri ->
                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            putExtra(android.content.Intent.EXTRA_STREAM, uri)
                            putExtra(android.content.Intent.EXTRA_SUBJECT, "Product Catalog")
                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(android.content.Intent.createChooser(intent, "Share Product"))
                        productViewModel.clearShareUri()
                    }
                }
                com.core2studio.mymanager.ui.screens.products.ProductsScreen(
                    categoryId = categoryId,
                    onBack = { navController.popBackStack() },
                    onAddProduct = { catId -> navController.navigate("add_product/$catId") },
                    onProductClick = { productId -> navController.navigate("product/$productId") },
                    onEditProduct = { productId -> navController.navigate("product/$productId") },
                    onAddToCart = { product -> cartViewModel.addToCart(product) },
                    onCartClick = { navController.navigate("cart") },
                    onShareProduct = { product -> productViewModel.shareProduct(context, product) },
                    onShareProducts = { products -> productViewModel.shareProducts(context, products) },
                    products = uiState.products,
                    categoryName = uiState.categoryName,
                    cartCount = cartViewModel.uiState.collectAsState().value.cartCount,
                    onDeleteProduct = { product -> productViewModel.deleteProduct(product) },
                    onDeleteProducts = { products -> productViewModel.deleteProducts(products) }
                )
            }

            composable(
                route = "add_product/{categoryId}",
                arguments = listOf(navArgument("categoryId") { type = NavType.StringType })
            ) { backStackEntry ->
                val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
                val context = androidx.compose.ui.platform.LocalContext.current
                com.core2studio.mymanager.ui.screens.products.AddProductScreen(
                    categoryId = categoryId,
                    onBack = { navController.popBackStack() },
                    onSave = { name, desc, price, imageUriStrings ->
                        val uris = imageUriStrings.map { android.net.Uri.parse(it) }
                        productViewModel.addProduct(categoryId, name, desc, price, uris, context)
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = "product/{productId}",
                arguments = listOf(navArgument("productId") { type = NavType.StringType })
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId") ?: ""
                val context = androidx.compose.ui.platform.LocalContext.current
                LaunchedEffect(productId) {
                    productViewModel.loadProductDetail(productId)
                }
                val uiState by productViewModel.uiState.collectAsState()
                com.core2studio.mymanager.ui.screens.products.ProductDetailScreen(
                    product = uiState.selectedProduct,
                    onBack = { navController.popBackStack() },
                    onUpdateProduct = { product -> productViewModel.updateProduct(product) },
                    onDeleteProduct = { product -> productViewModel.deleteProduct(product) },
                    onAddImages = { uris ->
                        uiState.selectedProduct?.let { product ->
                            productViewModel.addImagesToProduct(product, uris, context)
                        }
                    },
                    onRemoveImage = { imageUrl ->
                        uiState.selectedProduct?.let { product ->
                            productViewModel.removeImageFromProduct(product, imageUrl)
                        }
                    },
                    onAddToCart = { product, quantity ->
                        cartViewModel.addToCart(product, quantity)
                        navController.popBackStack()
                    }
                )
            }

            composable("cart") {
                val cartShareUiState by cartViewModel.shareUiState.collectAsState()
                val context = androidx.compose.ui.platform.LocalContext.current
                androidx.compose.runtime.LaunchedEffect(cartShareUiState.shareUri) {
                    cartShareUiState.shareUri?.let { uri ->
                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            putExtra(android.content.Intent.EXTRA_STREAM, uri)
                            putExtra(android.content.Intent.EXTRA_SUBJECT, "Cart Summary")
                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(android.content.Intent.createChooser(intent, "Share Cart"))
                        cartViewModel.clearShareUri()
                    }
                }
                CartScreen(
                    cartViewModel = cartViewModel,
                    onBack = { navController.popBackStack() },
                    onCheckout = { navController.navigate("checkout") }
                )
            }

            composable("checkout") {
                val orderUiState by orderViewModel.uiState.collectAsState()
                CheckoutScreen(
                    cartViewModel = cartViewModel,
                    clients = orderUiState.allClients,
                    onBack = { navController.popBackStack() },
                    onAddClient = { name, phone, email, address ->
                        orderViewModel.addClient(name, phone, email, address)
                    }
                )
            }

            composable("orders") {
                val uiState by orderViewModel.uiState.collectAsState()
                com.core2studio.mymanager.ui.screens.orders.OrdersScreen(
                    onAddOrder = { navController.navigate("add_order") },
                    onOrderClick = { orderId -> navController.navigate("order/$orderId") },
                    orders = uiState.orders,
                    searchQuery = uiState.searchQuery,
                    filteredOrders = uiState.filteredOrders,
                    onSearchQueryChange = { orderViewModel.onSearchQueryChange(it) },
                    clientNameResolver = { clientId ->
                        uiState.clients[clientId]?.name ?: "Unknown"
                    }
                )
            }

            composable(
                route = "order/{orderId}",
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                val uiState by orderViewModel.uiState.collectAsState()
                val order = uiState.orders.find { it.id == orderId }
                val clientName = order?.let { uiState.clients[it.clientId]?.name ?: "Unknown" } ?: "Unknown"
                com.core2studio.mymanager.ui.screens.orders.OrderDetailScreen(
                    order = order,
                    clientName = clientName,
                    onBack = { navController.popBackStack() },
                    onStatusUpdate = { updatedOrder -> orderViewModel.updateOrder(updatedOrder) },
                    onGenerateInvoice = { ord -> orderViewModel.generateInvoice(ord) },
                    invoiceUri = uiState.invoiceUri,
                    invoiceMessage = uiState.invoiceMessage,
                    onClearUri = { orderViewModel.clearInvoiceUri() },
                    onClearMessage = { orderViewModel.clearInvoiceMessage() }
                )
            }

            composable("add_order") {
                val uiState by orderViewModel.uiState.collectAsState()
                com.core2studio.mymanager.ui.screens.orders.AddOrderScreen(
                    onBack = { navController.popBackStack() },
                    clients = uiState.allClients,
                    onAddClient = { name, phone, email, address ->
                        orderViewModel.addClient(name, phone, email, address)
                    },
                    onSave = { clientId, productName, quantity, unitPrice, amount, paid, status, paymentMethod, notes, customFields ->
                        orderViewModel.addOrder(
                            clientId,
                            productName,
                            quantity,
                            unitPrice,
                            amount,
                            paid,
                            status,
                            paymentMethod,
                            customFields,
                            notes
                        )
                        navController.popBackStack()
                    }
                )
            }

            composable("clients") {
                val uiState by clientViewModel.uiState.collectAsState()
                com.core2studio.mymanager.ui.screens.clients.ClientsScreen(
                    onClientClick = { clientId -> navController.navigate("client/$clientId") },
                    clients = uiState.clients,
                    searchQuery = uiState.searchQuery,
                    filteredClients = uiState.filteredClients,
                    onSearchQueryChange = { clientViewModel.onSearchQueryChange(it) },
                    showAddDialog = uiState.showAddDialog,
                    onShowAddDialog = { clientViewModel.showAddDialog() },
                    onDismissAddDialog = { clientViewModel.dismissDialog() },
                    onAddClient = { name, phone, email, address ->
                        clientViewModel.addClient(name, phone, email, address)
                    }
                )
            }

            composable(
                route = "client/{clientId}",
                arguments = listOf(navArgument("clientId") { type = NavType.StringType })
            ) { backStackEntry ->
                val clientId = backStackEntry.arguments?.getString("clientId") ?: ""
                val uiState by clientViewModel.uiState.collectAsState()
                androidx.compose.runtime.LaunchedEffect(clientId) {
                    clientViewModel.loadClientDetail(clientId)
                }
                LaunchedEffect(settingsUiState.businessName, settingsUiState.businessEmail, settingsUiState.businessPhone, settingsUiState.businessAddress, settingsUiState.website, settingsUiState.gstin) {
                    clientViewModel.setBusinessInfo(
                        name = settingsUiState.businessName,
                        email = settingsUiState.businessEmail,
                        phone = settingsUiState.businessPhone,
                        address = settingsUiState.businessAddress,
                        website = settingsUiState.website,
                        gstin = settingsUiState.gstin
                    )
                }
                com.core2studio.mymanager.ui.screens.clients.ClientDetailScreen(
                    clientId = clientId,
                    onBack = { navController.popBackStack() },
                    client = uiState.selectedClient,
                    transactions = uiState.clientTransactions,
                    onGenerateInvoice = { transaction -> clientViewModel.generateInvoice(transaction) },
                    invoiceMessage = uiState.invoiceMessage,
                    onClearMessage = { clientViewModel.clearInvoiceMessage() },
                    invoiceUri = uiState.invoiceUri,
                    onClearUri = { clientViewModel.clearInvoiceUri() },
                    onEditClient = { uiState.selectedClient?.let { clientViewModel.showEditDialog(it) } },
                    showEditDialog = uiState.showEditDialog,
                    editClient = uiState.showEditDialogClient,
                    onDismissEditDialog = { clientViewModel.dismissEditDialog() },
                    onConfirmEdit = { name, phone, email, address ->
                        clientViewModel.updateClient(clientId, name, phone, email, address)
                    }
                )
            }

            composable("settings") {
                val uiState by settingsViewModel.uiState.collectAsState()
                val onBusinessInfoClick = remember { { navController.navigate("business_info") } }
                val onProfileClick = remember { { navController.navigate("user_profile") } }
                val onSettingsClick = remember { { navController.navigate("app_settings") } }
                val onSignOutApp = remember {
                    {
                        settingsViewModel.signOut()
                        authViewModel.signOut()
                    }
                }
                com.core2studio.mymanager.ui.screens.settings.SettingsScreen(
                    onBusinessInfoClick = onBusinessInfoClick,
                    onProfileClick = onProfileClick,
                    onSettingsClick = onSettingsClick,
                    accountEmail = uiState.accountEmail,
                    message = uiState.message,
                    onSignOutApp = onSignOutApp
                )
            }

            composable("app_settings") {
                val uiState by settingsViewModel.uiState.collectAsState()
                com.core2studio.mymanager.ui.screens.settings.AppSettingsScreen(
                    themeMode = uiState.themeMode,
                    onThemeModeSelected = { settingsViewModel.saveThemeMode(it) },
                    currencyCode = uiState.currencyCode,
                    onCurrencySelected = { settingsViewModel.saveCurrency(it) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("business_info") {
                val uiState by settingsViewModel.uiState.collectAsState()
                com.core2studio.mymanager.ui.screens.settings.BusinessInfoScreen(
                    businessName = uiState.businessName,
                    businessEmail = uiState.businessEmail,
                    businessPhone = uiState.businessPhone,
                    businessAddress = uiState.businessAddress,
                    businessLogoUrl = uiState.businessLogoUrl,
                    gstin = uiState.gstin,
                    website = uiState.website,
                    isUploadingLogo = uiState.isUploadingLogo,
                    onSaveBusinessInfo = { name, email, phone, address, gstin, website ->
                        settingsViewModel.saveBusinessInfo(name, email, phone, address, gstin = gstin, website = website)
                    },
                    onUploadLogo = { uri -> settingsViewModel.uploadLogo(uri) },
                    onClearLogo = { settingsViewModel.clearLogo() },
                    onViewBusinessCard = { navController.navigate("business_card") },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("business_card") {
                val uiState by settingsViewModel.uiState.collectAsState()
                com.core2studio.mymanager.ui.screens.settings.BusinessCardScreen(
                    businessName = uiState.businessName,
                    businessEmail = uiState.businessEmail,
                    businessPhone = uiState.businessPhone,
                    businessAddress = uiState.businessAddress,
                    businessLogoUrl = uiState.businessLogoUrl,
                    gstin = uiState.gstin,
                    website = uiState.website,
                    displayName = authViewModel.getDisplayName(),
                    onBack = { navController.popBackStack() }
                )
            }

            composable("user_profile") {
                val authUiState by authViewModel.uiState.collectAsState()
                com.core2studio.mymanager.ui.screens.settings.UserProfileScreen(
                    displayName = authUiState.displayName,
                    email = authUiState.email,
                    isLoading = authUiState.isLoading,
                    message = authUiState.error,
                    onUpdateDisplayName = { newName ->
                        authViewModel.updateDisplayName(newName)
                    },
                    onChangePassword = { currentPassword, newPassword ->
                        authViewModel.changePassword(currentPassword, newPassword)
                    },
                    onClearMessage = { authViewModel.clearError() },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
