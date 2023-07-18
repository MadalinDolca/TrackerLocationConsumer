package com.madalin.trackerlocationconsumer.entity

/**
 * The state of the login functionality withing the app.
 * It holds information related to the user.
 */
data class AppLoginState(
    val isLoggedIn: Boolean = false,
    val username: String = "",
    val email: String = "",
    val lastError: String? = null
)
