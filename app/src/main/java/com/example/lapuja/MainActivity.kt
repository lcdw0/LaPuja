package com.example.lapuja

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.lapuja.components.BottomNav
import com.example.lapuja.data.AuctionItem
import com.example.lapuja.data.Bid
import com.example.lapuja.ui.screens.AuctionDetailScreen
import com.example.lapuja.ui.screens.AuctionScreen
import com.example.lapuja.ui.screens.CreateAuctionScreen
import com.example.lapuja.ui.screens.EditProfileScreen
import com.example.lapuja.ui.screens.HistoryScreen
import com.example.lapuja.ui.screens.HomeScreen
import com.example.lapuja.ui.screens.LoginScreen
import com.example.lapuja.ui.screens.MyAuctionsScreen
import com.example.lapuja.ui.screens.MyBidsScreen
import com.example.lapuja.ui.screens.PaymentMethodsScreen
import com.example.lapuja.ui.screens.ProfileScreen
import com.example.lapuja.ui.screens.RegisterScreen
import com.example.lapuja.ui.theme.LaPujaTheme

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

        enableEdgeToEdge()

        setContent {
            LaPujaTheme {
                MainScreen(prefs = prefs)
            }
        }
    }
}

@Composable
fun MainScreen(prefs: SharedPreferences) {
    val navController = rememberNavController()

    val historial = remember {
        mutableStateListOf<Bid>()
    }

    var selectedAuction by remember {
        mutableStateOf<AuctionItem?>(null)
    }

    val usuarioGuardado = prefs.getString("correo", null)

    val startDestination = if (usuarioGuardado == null) {
        "login"
    } else {
        "home"
    }

    Scaffold(
        bottomBar = {
            val currentRoute = navController
                .currentBackStackEntryAsState()
                .value
                ?.destination
                ?.route

            if (
                currentRoute != "login" &&
                currentRoute != "register" &&
                currentRoute != "auction_detail"
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

            composable("home") {
                HomeScreen(
                    navController = navController
                )
            }

            composable("auction") {
                AuctionScreen(
                    prefs = prefs,
                    historial = historial,
                    onAuctionClick = { auction ->
                        selectedAuction = auction
                        navController.navigate("auction_detail")
                    }
                )
            }

            composable("create_auction") {
                CreateAuctionScreen()
            }

            composable("auction_detail") {
                selectedAuction?.let { auction ->
                    AuctionDetailScreen(
                        auction = auction,
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }
            }

            composable("history") {
                HistoryScreen(
                    historial = historial
                )
            }

            composable("profile") {
                ProfileScreen(
                    prefs = prefs,
                    navController = navController
                )
            }

            composable("edit_profile") {
                EditProfileScreen()
            }

            composable("my_auctions") {
                MyAuctionsScreen()
            }

            composable("my_bids") {
                MyBidsScreen()
            }

            composable("payment_methods") {
                PaymentMethodsScreen()
            }
        }
    }
}