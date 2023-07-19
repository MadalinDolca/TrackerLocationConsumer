package com.madalin.trackerlocationconsumer.feature.auth.login

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madalin.trackerlocationconsumer.entity.AppState
import com.madalin.trackerlocationconsumer.model.AppStateDriver
import com.madalin.trackerlocationconsumer.repository.FirebaseRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val stateDriver: AppStateDriver, // constructor injection
    private val repository: FirebaseRepositoryImpl
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
     * Handles the given [AppLoginAction] via [AppStateDriver.handleAction].
     * @param loginAction action to handle
     */
    /*fun handleApplicationAction(loginAction: AppLoginAction) {
        stateDriver.handleAction(loginAction)
    }*/

    /**
     * Handles the given [LoginAction].
     * @param loginAction action to handle
     */
    fun handleLoginAction(loginAction: LoginAction) {
        when (loginAction) {
            is LoginAction.UpdateEmailTextField -> viewStateInternal.update {
                it.copy(email = loginAction.email)
            }

            is LoginAction.UpdatePasswordTextField -> viewStateInternal.update {
                it.copy(password = loginAction.password)
            }

            is LoginAction.DoLogin -> login(loginAction.email, loginAction.password)

            is LoginAction.OnLoginSuccess -> viewStateInternal.update {
                it.copy(
                    email = loginAction.email,
                    isLoggedIn = true
                )
            }

            is LoginAction.OnLoginFailure -> viewStateInternal.update {
                it.copy(
                    isLoggedIn = false,
                    errorMessage = loginAction.errorMessage
                )
            }

            LoginAction.DoLogout -> {
                repository.signOut()
                viewStateInternal.update {
                    it.copy(
                        email = "",
                        password = "",
                        isLoggedIn = false,
                        errorMessage = null
                    )
                }
            }
        }
    }

    /**
     * Validates and signs in the user with the given [email] and [password]. If the login succeeds
     * it will launch a [LoginAction.OnLoginSuccess] action, [LoginAction.OnLoginFailure] otherwise.
     *
     * @param email given email
     * @param password given password
     */
    private fun login(email: String, password: String) {
        val _email = email.trim()
        val _password = password

        // if given data is valid
        if (validateFields(_email, _password)) {
            repository.signInWithEmailAndPassword(_email, _password) { isSuccess, errorMessage ->
                if (isSuccess) {
                    handleLoginAction(LoginAction.OnLoginSuccess(email = _email))
                } else {
                    errorMessage?.let { handleLoginAction(LoginAction.OnLoginFailure(it)) }
                }
            }
        }
    }

    /**
     * Checks if the given data for authentication is valid. If not, it will launch [LoginAction.OnLoginFailure].
     * @param email given email
     * @param password given password
     * @return `true` if valid, `false` otherwise
     */
    private fun validateFields(email: String, password: String): Boolean {
        when {
            //email
            email.isEmpty() -> {
                handleLoginAction(LoginAction.OnLoginFailure(errorMessage = "Email can't be empty"))
                return false
            }

            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                handleLoginAction(LoginAction.OnLoginFailure(errorMessage = "Email is invalid"))
                return false
            }

            // password
            password.isEmpty() -> {
                handleLoginAction(LoginAction.OnLoginFailure(errorMessage = "Password can't be empty"))
                return false
            }

            password.length < 6 -> {
                handleLoginAction(LoginAction.OnLoginFailure(errorMessage = "Password is too short (min 6 characters)"))
                return false
            }
        }

        return true
    }
}