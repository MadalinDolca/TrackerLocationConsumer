package com.madalin.trackerlocationconsumer.feature.auth

import com.madalin.trackerlocationconsumer.entity.Action

sealed class SignUpScreenAction : Action {
    data class UpdateUsernameTextField(val username: String) : SignUpScreenAction()
    data class UpdateEmailTextField(val email: String) : SignUpScreenAction()
    data class UpdatePasswordTextField(val password: String) : SignUpScreenAction()
    data class CreateAccount(val username: String, val email: String, val password: String) : SignUpScreenAction()
    object OnSignUpSuccess : SignUpScreenAction()
    data class OnSignUpFailure(val errorMessage: String) : SignUpScreenAction()
}