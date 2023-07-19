package com.madalin.trackerlocationconsumer.feature.auth.login

data class LoginViewState(
    val email: String = "",
    val password: String = "",
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null
)