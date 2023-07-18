package com.madalin.trackerlocationconsumer.entity

import android.util.Patterns
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.madalin.trackerlocationconsumer.feature.tracker.TrackerAction
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
class AppStateDriver {
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
            // updates the ApplicationState (if loginAction is an instance of the LoginAction.DoLogin class)
            is LoginAction.DoLogin -> login(loginAction.email, loginAction.password)

            // logout action (type checking not necessary)
            LoginAction.DoLogout -> stateInternal.update { oldLoginState ->
                oldLoginState.copy(
                    loginState = AppLoginState(), // marked as "not logged in"
                    trackingState = emptyList(),
                    coordinatesState = emptyList()
                )
            }

            is LoginAction.OnLoginSuccess -> {
                stateInternal.update { oldLoginState ->
                    oldLoginState.copy(
                        loginState = AppLoginState( // marked as logged in and set user's name
                            isLoggedIn = true,
                            email = loginAction.email
                        ),
                        trackingState = emptyList(),
                        coordinatesState = emptyList()
                    )
                }
            }

            is LoginAction.OnLoginFailure -> {}
        }
    }

    private fun handleTrackerAction(trackerAction: TrackerAction) {

    }

    fun login(email: String, password: String) {
        val auth = Firebase.auth
        val _email = email.trim()
        val _password = password

        // if given data is valid
        if (validateFields(_email, _password)) {
            auth.signInWithEmailAndPassword(_email, _password)
                .addOnCompleteListener { signInTask ->
                    // if the authentication was successful
                    if (signInTask.isSuccessful) {
                        handleAction(LoginAction.OnLoginSuccess(email = _email))
                    }
                }
                .addOnFailureListener {
                    handleAction(LoginAction.OnLoginFailure(errorMessage = it.message.toString()))
                }
        }
    }

    /**
     * Checks if the given data for authentication is valid.
     * @param email given email
     * @param password given password
     * @return `true` if valid, `false` otherwise
     */
    fun validateFields(email: String, password: String): Boolean {
        when {
            //email
            email.isEmpty() -> {
                handleAction(LoginAction.OnLoginFailure(errorMessage = "Email can't be empty"))
                return false
            }

            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                handleAction(LoginAction.OnLoginFailure(errorMessage = "Email is invalid"))
                return false
            }

            // password
            password.isEmpty() -> {
                handleAction(LoginAction.OnLoginFailure(errorMessage = "Password can't be empty"))
                return false
            }

            password.length < 6 -> {
                handleAction(LoginAction.OnLoginFailure(errorMessage = "Password is too short (min 6 characters)"))
                return false
            }
        }

        return true
    }
}