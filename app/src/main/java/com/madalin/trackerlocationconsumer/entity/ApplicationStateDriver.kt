package com.madalin.trackerlocationconsumer.entity

import android.util.Log
import com.madalin.trackerlocationconsumer.feature.login.LoginAction
import com.madalin.trackerlocationconsumer.feature.login.LoginState
import com.madalin.trackerlocationconsumer.feature.tracker.TrackerAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Used to manage and control the application state. It acts as a mediator between different actions
 * and the underlying state of the application.
 * Used in ViewModels.
 */
class ApplicationStateDriver {
    private val stateInternal = MutableStateFlow(ApplicationState()) // the overall state of the application
    private val coroutineScope = CoroutineScope(Dispatchers.Default) // for coroutines execution in the background thread pool
    private val stateMutex = Mutex() // mutual exclusion for coroutines (locked and unlocked states)
    //private val appStateContext = newSingleThreadContext("AppStateContext")

    val state = stateInternal.asSharedFlow() // shared state to allow components to observe the state and react to state changes

    /**
     * Determines the [Action] type and calls the appropriate handle method with this [action] as
     * a parameter.
     * @param action to handle
     */
    fun handleAction(action: Action) {
        coroutineScope.launch {
            stateMutex.withLock { // acquire the lock to ensure exclusive access to the state // sync(mutex) if I want result checking
                when (action) {
                    is LoginAction -> handleLoginAction(action)
                    is TrackerAction -> handleTrackerAction(action)
                }
            }
        }
    }

    /**
     * Determines the [LoginAction] type and applies the operations.
     */
    private fun handleLoginAction(loginAction: LoginAction) {
        when (loginAction) {
            // updates the ApplicationState (if loginAction is an instance of the LoginAction.DoLogin class)
            is LoginAction.DoLogin -> stateInternal.update { oldLoginState ->
                oldLoginState.copy(
                    loginState = LoginState( // marked as logged in and set user's name
                        isLoggedIn = true,
                        name = loginAction.username
                    ),
                    trackingState = emptyList(),
                    coordinatesState = emptyList()
                )
            }

            // logout action (type checking not necessary)
            LoginAction.DoLogout -> stateInternal.update { oldLoginState ->
                oldLoginState.copy(
                    loginState = LoginState(), // marked as "not logged in"
                    trackingState = emptyList(),
                    coordinatesState = emptyList()
                )
            }

            is LoginAction.OnLoginSuccess -> {}
            is LoginAction.OnLoginFailure -> {}
        }
    }

    private fun handleTrackerAction(trackerAction: TrackerAction) {

    }
}