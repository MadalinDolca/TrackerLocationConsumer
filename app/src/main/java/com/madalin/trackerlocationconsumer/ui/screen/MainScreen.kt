package com.madalin.trackerlocationconsumer.ui.screen

import androidx.compose.runtime.Composable
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.madalin.trackerlocationconsumer.ui.screen.destinations.LoginScreenDestination
import com.madalin.trackerlocationconsumer.ui.screen.destinations.TrackerScreenDestination
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@RootNavGraph(start = true) // start destination
@Destination
@Composable
fun MainScreen(navigator: DestinationsNavigator) {
    val auth = Firebase.auth.currentUser

    // if user is connected, redirects it to TrackerScreen
    if (auth != null) {
        navigator.navigate(TrackerScreenDestination)
    } else { // else to LoginScreen
        navigator.navigate(LoginScreenDestination)
    }

    /*val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.Login.route) {
        composable(Routes.Login.route) { LoginScreen(navController = navController) }
        composable(Routes.SignUp.route) { SignUpScreen(navController = navController) }

        composable(Routes.PasswordReset.route) { navBackStack ->
            PasswordResetScreen(navController = navController)
        }

        composable(Routes.Tracker.route) { TrackerScreen(navController = navController) }
    }*/
}