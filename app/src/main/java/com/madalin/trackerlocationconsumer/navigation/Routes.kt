package com.madalin.trackerlocationconsumer.navigation

sealed class Routes(val route: String) {
    object SignUp : Routes("auth/signup")
    object Login : Routes("auth/login")
    object PasswordReset : Routes("auth/reset")

    object Tracker : Routes("tracker")
}
