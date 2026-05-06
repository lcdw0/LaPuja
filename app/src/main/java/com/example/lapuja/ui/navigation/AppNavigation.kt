package com.example.lapuja.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.example.lapuja.ui.screen.CreateAuctionScreen
import com.example.lapuja.ui.screen.HomeScreen
import com.example.lapuja.ui.screen.LoginScreen
import com.example.lapuja.ui.screen.ProfileScreen
import com.example.lapuja.ui.screen.RegisterScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginClick = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onRegisterClick = {
                    navController.navigate(Routes.REGISTER)
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterClick = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.HOME) {
            MainScreen(navController)
        }
    }
}

@Composable
fun MainScreen(navController: NavHostController) {
    val bottomNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = false,
                    onClick = { bottomNavController.navigate(Routes.HOME) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") }
                )

                NavigationBarItem(
                    selected = false,
                    onClick = { bottomNavController.navigate(Routes.CREATE_AUCTION) },
                    icon = { Icon(Icons.Default.AddCircle, contentDescription = "Crear Subasta") },
                    label = { Text("Crear") }
                )

                NavigationBarItem(
                    selected = false,
                    onClick = { bottomNavController.navigate(Routes.PROFILE) },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") }
                )
            }
        }
    ) { paddingValues ->

        NavHost(
            navController = bottomNavController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onAuctionClick = {
                        navController.navigate(Routes.AUCTION_DETAIL)
                    }
                )
            }

            composable(Routes.CREATE_AUCTION) {
                CreateAuctionScreen()
            }

            composable(Routes.PROFILE) {
                ProfileScreen(
                    onLogoutClick = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}