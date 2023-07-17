package com.madalin.trackerlocationconsumer.feature.login.ui

data class LoginViewState(
    val username: String = "",
    val password: String = "",
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null
)