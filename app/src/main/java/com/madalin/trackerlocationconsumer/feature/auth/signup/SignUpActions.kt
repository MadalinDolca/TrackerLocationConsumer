package com.madalin.trackerlocationconsumer.feature.auth.signup

import com.madalin.trackerlocationconsumer.entity.Action

sealed class SignUpAction : Action {
    data class UpdateUsernameTextField(val username: String) : SignUpAction()
    data class UpdateEmailTextField(val email: String) : SignUpAction()
    data class UpdatePasswordTextField(val password: String) : SignUpAction()
    data class CreateAccount(val username: String, val email: String, val password: String) : SignUpAction()
    object OnSignUpSuccess : SignUpAction()
    data class OnSignUpFailure(val errorMessage: String) : SignUpAction()
}