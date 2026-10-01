package com.irys.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.irys.app.feature.chats.ChatsScreen
import com.irys.app.feature.emergency.EmergencyScreen
import com.irys.app.feature.home.HomeScreen
import com.irys.app.feature.nearby.NearbyScreen
import com.irys.app.feature.onboarding.OnboardingScreen
import com.irys.app.feature.permissions.PermissionsScreen
import com.irys.app.feature.settings.SettingsScreen
import com.irys.app.feature.splash.SplashScreen

@Composable
fun IrysNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Routes.SPLASH
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onNavigateToOnboarding = {
                    navController.navigate(Routes.ONBOARDING) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToPermissions = {
                    navController.navigate(Routes.PERMISSIONS) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(Routes.PERMISSIONS) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.PERMISSIONS) {
            PermissionsScreen(
                onContinue = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.PERMISSIONS) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToNearby = { navController.navigate(Routes.NEARBY) },
                onNavigateToChats = { navController.navigate(Routes.CHATS) },
                onNavigateToEmergency = { navController.navigate(Routes.EMERGENCY) }
            )
        }

        composable(Routes.NEARBY) {
            NearbyScreen()
        }

        composable(Routes.CHATS) {
            ChatsScreen()
        }

        composable(Routes.EMERGENCY) {
            EmergencyScreen()
        }

        composable(Routes.SETTINGS) {
            SettingsScreen()
        }
    }
}
