package com.madalin.trackerlocationconsumer.feature.login

/**
 * The state of the login functionality withing the app.
 * It holds information related to the user.
 */
data class LoginState(
    val isLoggedIn: Boolean = false,
    val name: String = "",
    val email: String = "",
    val lastError: String? = null
)
