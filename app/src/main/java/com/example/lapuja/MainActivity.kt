package com.example.lapuja

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lapuja.components.BottomNav
import com.example.lapuja.data.AuctionItem
import com.example.lapuja.data.Bid
import com.example.lapuja.ui.screens.*
import com.example.lapuja.ui.theme.LaPujaTheme
import com.example.lapuja.ui.dashboard.DashboardScreen
import com.example.lapuja.ui.screens.PublicProfileScreen
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import com.example.lapuja.components.NotificationBell
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lapuja.ui.notifications.NotificationViewModel
import android.net.Uri
import androidx.compose.runtime.LaunchedEffect
import com.example.lapuja.ui.navigation.Routes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

class MainActivity : ComponentActivity() {

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("app", MODE_PRIVATE)

        if (!prefs.contains("saldo")) {
            prefs.edit()
                .putFloat("saldo", 10000.0f)
                .apply()
        }

        val deepLinkUri = intent?.data

        enableEdgeToEdge()

        setContent {
            LaPujaTheme {
                MainScreen(
                    prefs = prefs,
                    deepLinkUri = deepLinkUri
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    prefs: SharedPreferences,
    deepLinkUri: Uri? = null
) {
    val navController = rememberNavController()

    LaunchedEffect(deepLinkUri) {
        deepLinkUri?.let { uri ->
            val token = uri.getQueryParameter("token")

            when (uri.host) {
                "verificar-correo" -> {
                    if (!token.isNullOrBlank()) {
                        navController.navigate("verify_email/$token")
                    }
                }

                "recuperar-password" -> {
                    if (!token.isNullOrBlank()) {
                        navController.navigate("reset_password/$token")
                    }
                }
            }
        }
    }

    val notificationViewModel: NotificationViewModel = viewModel()

    val historial = remember {
        mutableStateListOf<Bid>()
    }

    val productos = remember {
        mutableStateListOf<AuctionItem>()
    }

    val usuarioGuardado = prefs.getString("correo", null)

    val startDestination = if (usuarioGuardado == null) {
        "login"
    } else {
        "home"
    }

    Scaffold(
        topBar = {
            val currentRoute = navController
                .currentBackStackEntryAsState()
                .value
                ?.destination
                ?.route

            if (
                currentRoute != "login" &&
                currentRoute != "register" &&
                currentRoute != "forgot_password" &&
                currentRoute?.startsWith("chat/") != true &&
                currentRoute?.startsWith("verify_email") != true &&
                currentRoute?.startsWith("reset_password") != true &&
                currentRoute != "notifications"
            ) {
                TopAppBar(
                    title = { Text("LaPuja") },
                    actions = {
                        IconButton(
                            onClick = {
                                navController.navigate("chat_list")
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Mensajes"
                            )
                        }

                        NotificationBell(
                            prefs = prefs,
                            navController = navController,
                            notificationViewModel = notificationViewModel
                        )

                        IconButton(
                            onClick = {
                                navController.navigate("profile")
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Perfil"
                            )
                        }
                    }
                )
            }
        },
        bottomBar = {
            val currentRoute = navController
                .currentBackStackEntryAsState()
                .value
                ?.destination
                ?.route

            if (
                currentRoute != "login" &&
                currentRoute != "register" &&
                currentRoute != "forgot_password" &&
                currentRoute?.startsWith("verify_email") != true &&
                currentRoute?.startsWith("reset_password") != true &&
                currentRoute != "notifications" &&
                currentRoute?.startsWith("auction_detail") != true &&
                currentRoute?.startsWith("public_profile") != true &&
                currentRoute?.startsWith("chat") != true
            ) {
                BottomNav(navController = navController)
            }
        }
    ) { paddingValues ->

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("login") {
                LoginScreen(
                    navController = navController,
                    prefs = prefs
                )
            }

            composable("register") {
                RegisterScreen(
                    navController = navController,
                    prefs = prefs
                )
            }

            composable(
                route = "verify_email/{token}",
                arguments = listOf(
                    navArgument("token") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val token = backStackEntry.arguments?.getString("token") ?: ""

                VerifyEmailScreen(
                    token = token,
                    navController = navController
                )
            }

            composable(
                route = "reset_password/{token}",
                arguments = listOf(
                    navArgument("token") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val token = backStackEntry.arguments?.getString("token") ?: ""

                ResetPasswordScreen(
                    token = token,
                    navController = navController
                )
            }

            composable("home") {
                HomeScreen(
                    navController = navController
                )
            }

            composable("auction") {
                AuctionScreen(
                    prefs = prefs,
                    historial = historial,
                    productos = productos,
                    onAuctionClick = { auction ->
                        navController.navigate("auction_detail/${auction.idApi}")
                    }
                )
            }

            composable("create_auction") {
                CreateAuctionScreen(
                    productos = productos,
                    onAuctionCreated = {
                        navController.navigate("auction") {
                            popUpTo("create_auction") {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            composable(
                route = "auction_detail/{subastaId}",
                arguments = listOf(
                    navArgument("subastaId") {
                        type = NavType.LongType
                    }
                )
            ) { backStackEntry ->

                val subastaId = backStackEntry.arguments?.getLong("subastaId") ?: 0L

                AuctionDetailScreen(
                    subastaId = subastaId,
                    prefs = prefs,
                    historial = historial,
                    navController = navController,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable("history") {
                HistoryScreen(
                    historial = historial,
                    navController = navController
                )
            }

            composable("profile") {
                ProfileScreen(
                    prefs = prefs,
                    navController = navController
                )
            }

            composable(Routes.WALLET) {
                WalletScreen(
                    prefs = prefs,
                    navController = navController
                )
            }

            composable("edit_profile") {
                EditProfileScreen(
                    prefs = prefs,
                    navController = navController
                )
            }

            composable("my_auctions") {
                MyAuctionsScreen(
                    productos = productos,
                    navController = navController
                )
            }

            composable("my_bids") {
                MyBidsScreen(
                    historial = historial,
                    navController = navController
                )
            }

            composable("payment_methods") {
                PaymentMethodsScreen()
            }

            composable("saved_auctions") {
                SavedAuctionsScreen(
                    navController = navController
                )
            }

            composable("recharge_wallet") {
                RechargeWalletScreen(
                    navController = navController,
                    prefs = prefs
                )
            }

            composable("wallet_history") {
                WalletHistoryScreen()
            }

            composable("held_funds") {
                HeldFundsScreen()
            }

            composable(
                route = "edit_auction/{subastaId}",
                arguments = listOf(
                    navArgument("subastaId") {
                        type = NavType.LongType
                    }
                )
            ) { backStackEntry ->

                val subastaId = backStackEntry.arguments?.getLong("subastaId") ?: 0L

                EditAuctionScreen(
                    subastaId = subastaId,
                    onAuctionUpdated = {
                        navController.navigate("my_auctions") {
                            popUpTo("edit_auction/{subastaId}") {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            fun obtenerUsuarioIdDesdePrefs(prefs: SharedPreferences): Long {
                return when (val id = prefs.all["usuarioId"]) {
                    is Long -> id
                    is Int -> id.toLong()
                    is String -> id.toLongOrNull() ?: 0L
                    else -> 0L
                }
            }

            composable("dashboard") {
                val usuarioId = obtenerUsuarioIdDesdePrefs(prefs)

                DashboardScreen(
                    usuarioId = usuarioId,
                    navController = navController
                )
            }

            composable(
                route = "public_profile/{usuarioId}",
                arguments = listOf(
                    navArgument("usuarioId") {
                        type = NavType.LongType
                    }
                )
            ) { backStackEntry ->

                val usuarioId = backStackEntry.arguments?.getLong("usuarioId") ?: 0L

                PublicProfileScreen(
                    usuarioId = usuarioId,
                    navController = navController
                )
            }

            composable("notifications") {
                NotificationsScreen(
                    prefs = prefs,
                    navController = navController,
                    notificationViewModel = notificationViewModel
                )
            }

            composable("chat_list") {
                ChatListScreen(
                    prefs = prefs,
                    onChatClick = { conversacionId ->
                        navController.navigate("chat/$conversacionId")
                    }
                )
            }

            composable(
                route = "chat/{conversacionId}",
                arguments = listOf(
                    navArgument("conversacionId") {
                        type = NavType.LongType
                    }
                )
            ) { backStackEntry ->

                val conversacionId =
                    backStackEntry.arguments?.getLong("conversacionId") ?: 0L

                ChatScreen(
                    prefs = prefs,
                    conversacionId = conversacionId,
                    navController = navController
                )
            }

            composable("forgot_password") {
                ForgotPasswordScreen(
                    navController = navController
                )
            }
        }
    }
}