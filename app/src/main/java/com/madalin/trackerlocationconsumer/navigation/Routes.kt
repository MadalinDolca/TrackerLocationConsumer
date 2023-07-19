package com.madalin.trackerlocationconsumer.navigation

@Deprecated("No longer used since Navigation was replaced with Compose Destinations.")
sealed class Routes(val route: String) {
    object SignUp : Routes("auth/signup")
    object Login : Routes("auth/login")
    object PasswordReset : Routes("auth/reset")

    object Tracker : Routes("tracker")
}
