package com.madalin.trackerlocationconsumer.model

import com.madalin.trackerlocationconsumer.entity.Action
import com.madalin.trackerlocationconsumer.entity.AppLoginState
import com.madalin.trackerlocationconsumer.entity.AppState
import com.madalin.trackerlocationconsumer.feature.auth.login.LoginAction
import com.madalin.trackerlocationconsumer.feature.tracker.TrackerAction
import com.madalin.trackerlocationconsumer.repository.FirebaseRepository
import com.madalin.trackerlocationconsumer.repository.FirebaseRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Used to manage and control the application state. It acts as a mediator between different actions
 * and the underlying state of the application.
 * Used in ViewModels.
 */
class AppStateDriver(
    val repository: FirebaseRepositoryImpl
) {
    private val stateInternal = MutableStateFlow(AppState()) // the overall state of the application
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
            LoginAction.DoLogout -> {
                repository.signOut()
                stateInternal.update {
                    it.copy(
                        loginState = AppLoginState(
                            isLoggedIn = false,
                            username = "",
                            email = ""
                        )
                    )
                }
            }

            else -> {}
        }
    }

    private fun handleTrackerAction(trackerAction: TrackerAction) {

    }
}