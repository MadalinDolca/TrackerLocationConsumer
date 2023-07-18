package com.madalin.trackerlocationconsumer.feature.auth

data class SignUpViewState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val hasRegistered: Boolean = false,
    val errorMessage: String? = null
)