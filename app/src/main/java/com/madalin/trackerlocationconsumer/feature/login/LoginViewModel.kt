package com.madalin.trackerlocationconsumer.feature.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madalin.trackerlocationconsumer.entity.ApplicationState
import com.madalin.trackerlocationconsumer.entity.ApplicationStateDriver
import com.madalin.trackerlocationconsumer.feature.login.ui.LoginViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val stateDriver: ApplicationStateDriver // constructor injection
) : ViewModel() {
    private val viewStateInternal = MutableStateFlow(LoginViewState()) // login view state
    val viewState = viewStateInternal.asStateFlow() // to read the state of the view

    init {
        viewModelScope.launch {
            stateDriver.state.collect { applicationState ->
                applicationState.reduce()
            }
        }
    }

    /**
     * Handles the given [LoginAction] via [ApplicationStateDriver.handleAction].
     * @param loginAction action to handle
     */
    fun handleAction(loginAction: LoginAction) {
        stateDriver.handleAction(loginAction)
    }

    /**
     * Updates [viewStateInternal] with the values obtained from [ApplicationState].
     */
    private fun ApplicationState.reduce() {
        viewStateInternal.update { oldLoginViewState ->
            oldLoginViewState.copy(
                isLoggedIn = this.loginState.isLoggedIn, // logged in status
                errorMessage = this.loginState.lastError // error message
            )
        }
    }
}