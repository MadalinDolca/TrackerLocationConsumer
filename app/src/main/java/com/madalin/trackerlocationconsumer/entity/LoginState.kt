package com.madalin.trackerlocationconsumer.entity

data class LoginState(
    val isLoggedIn: Boolean = false,
    val name: String = "",
    val email: String = "",
    val lastError: String? = null
)
