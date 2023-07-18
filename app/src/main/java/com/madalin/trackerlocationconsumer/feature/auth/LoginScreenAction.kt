package com.madalin.trackerlocationconsumer.feature.auth

import com.madalin.trackerlocationconsumer.entity.Action

sealed class LoginScreenAction : Action {
    data class UpdateEmailTextField(val email: String) : LoginScreenAction()
    data class UpdatePasswordTextField(val password: String) : LoginScreenAction()
}
