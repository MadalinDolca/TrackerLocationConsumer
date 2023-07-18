package com.madalin.trackerlocationconsumer.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madalin.trackerlocationconsumer.entity.AppState
import com.madalin.trackerlocationconsumer.model.AppStateDriver
import com.madalin.trackerlocationconsumer.entity.LoginAction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val stateDriver: AppStateDriver // constructor injection
) : ViewModel() {
    private val viewStateInternal = MutableStateFlow(LoginViewState()) // login view state
    val viewState = viewStateInternal.asStateFlow() // to read the state of the view

    init {
        // continuously collect the states
        viewModelScope.launch {
            stateDriver.state.collect { applicationState ->
                applicationState.reduce()
            }
        }
    }

    /**
     * Updates [viewStateInternal] with the values obtained from [AppState].
     */
    private fun AppState.reduce() {
        viewStateInternal.update { oldLoginViewState ->
            oldLoginViewState.copy(
                isLoggedIn = this.loginState.isLoggedIn, // logged in status
                errorMessage = this.loginState.lastError // error message
            )
        }
    }

    /**
     * Handles the given [LoginAction] via [AppStateDriver.handleAction].
     * @param loginAction action to handle
     */
    fun handleAction(loginAction: LoginAction) {
        stateDriver.handleAction(loginAction)
    }

    fun handleLoginScreenAction(loginAction: LoginScreenAction) {
        when (loginAction) {
            is LoginScreenAction.UpdateEmailTextField -> {
                viewStateInternal.update { it.copy(email = loginAction.email) }
            }

            is LoginScreenAction.UpdatePasswordTextField -> {
                viewStateInternal.update { it.copy(password = loginAction.password) }
            }
        }
    }
}