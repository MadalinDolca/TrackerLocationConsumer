package com.madalin.trackerlocationconsumer.ui.screen

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.madalin.trackerlocationconsumer.navigation.Routes

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.Login.route) {
        composable(Routes.Login.route) { LoginScreen(navController = navController) }
        composable(Routes.SignUp.route) { SignUpScreen(navController = navController) }

        composable(Routes.PasswordReset.route) { navBackStack ->
            PasswordResetScreen(navController = navController)
        }

        composable(Routes.Tracker.route) { TrackerScreen(navController = navController) }
    }
}