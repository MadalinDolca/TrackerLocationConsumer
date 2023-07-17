package com.madalin.trackerlocationconsumer.feature.login

import com.madalin.trackerlocationconsumer.entity.Action

sealed class LoginAction : Action {
    data class DoLogin(
        val username: String,
        val password: String
    ) : LoginAction()

    data class OnLoginSuccess(
        val name: String,
        val email: String
    ) : LoginAction()

    data class OnLoginFailure(
        val errorMessage: String
    ) : LoginAction()

    object DoLogout : LoginAction()
}