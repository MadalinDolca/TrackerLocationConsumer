package com.madalin.trackerlocationconsumer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madalin.trackerlocationconsumer.entity.LoginAction
import com.madalin.trackerlocationconsumer.entity.State
import com.madalin.trackerlocationconsumer.model.StateDriver
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val viewStateInternal = MutableStateFlow(LoginViewState())
    val viewState = viewStateInternal.asStateFlow()
    lateinit var stateDriver: StateDriver
//koin
    init {
        viewModelScope.launch {
            stateDriver.state.collect {
                it.reduce()
            }
        }
    }

    fun handleAction(loginAction: LoginAction) {
        stateDriver.handleAction(loginAction)
    }

    fun State.reduce() {
        viewStateInternal.update { oldViewState ->
            oldViewState.copy(
                isLoggedIn = this.loginState.isLoggedIn,
                errorMessage = this.loginState.lastError
            )
        }
    }
}

data class LoginViewState(
    val username: String = "",
    val password: String = "",
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null
)