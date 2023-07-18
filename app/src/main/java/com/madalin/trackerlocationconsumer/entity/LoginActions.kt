package com.madalin.trackerlocationconsumer.entity

sealed class LoginAction : Action {
    data class DoLogin(
        val email: String,
        val password: String
    ) : LoginAction()

    data class OnLoginSuccess(
        //val username: String,
        val email: String
    ) : LoginAction()

    data class OnLoginFailure(
        val errorMessage: String
    ) : LoginAction()

    object DoLogout : LoginAction()
}