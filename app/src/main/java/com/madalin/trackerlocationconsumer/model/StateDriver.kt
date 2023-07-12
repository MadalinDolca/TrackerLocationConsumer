package com.madalin.trackerlocationconsumer.model

import com.madalin.trackerlocationconsumer.entity.Action
import com.madalin.trackerlocationconsumer.entity.LoginAction
import com.madalin.trackerlocationconsumer.entity.LoginState
import com.madalin.trackerlocationconsumer.entity.State
import com.madalin.trackerlocationconsumer.entity.TrackingAction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update

class StateDriver {
    private val stateInternal = MutableStateFlow(State())
    val state = stateInternal.asSharedFlow()

    fun handleAction(action: Action) {
        when (action) {
            is LoginAction -> handleLoginAction(action)
            is TrackingAction -> handleTrackingAction(action)
        }
    }

    private fun handleLoginAction(loginAction: LoginAction) {
        when (loginAction) {
            is LoginAction.DoLogin -> stateInternal.update { old ->
                old.copy(
                    loginState = LoginState(
                        isLoggedIn = true,
                        name = loginAction.username
                    ),
                    trackingState = emptyList(),
                    coordinatesState = emptyList()
                )
            }

            LoginAction.DoLogout -> stateInternal.update { old ->
                old.copy(
                    loginState = LoginState(),
                    trackingState = emptyList(),
                    coordinatesState = emptyList()
                )
            }

            is LoginAction.OnLoginSuccess -> {}
            is LoginAction.OnLoginFailure -> {}
        }
    }

    private fun handleTrackingAction(trackingAction: TrackingAction) {

    }
}