package com.madalin.trackerlocationconsumer.feature.auth.login

import com.madalin.trackerlocationconsumer.entity.Action

sealed class LoginAction : Action {
    data class UpdateEmailTextField(val email: String) : LoginAction()
    data class UpdatePasswordTextField(val password: String) : LoginAction()
    data class DoLogin(val email: String, val password: String) : LoginAction()
    data class OnLoginSuccess(val email: String) : LoginAction()
    data class OnLoginFailure(val errorMessage: String) : LoginAction()
    object DoLogout : LoginAction()
}