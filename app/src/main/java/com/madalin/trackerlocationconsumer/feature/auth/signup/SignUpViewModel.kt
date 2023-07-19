package com.madalin.trackerlocationconsumer.feature.auth.signup

import android.util.Patterns
import androidx.lifecycle.ViewModel
import com.madalin.trackerlocationconsumer.model.User
import com.madalin.trackerlocationconsumer.repository.FirebaseRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SignUpViewModel(
    private val repository: FirebaseRepositoryImpl
) : ViewModel() {
    private val viewStateInternal = MutableStateFlow(SignUpViewState())
    val viewState = viewStateInternal.asStateFlow()

    /**
     * Determines the [SignUpAction] type and calls the appropriate handle method with this
     * [signUpAction] as a parameter.
     * @param signUpAction action to handle
     */
    fun handleSignUpAction(signUpAction: SignUpAction) {
        when (signUpAction) {
            is SignUpAction.UpdateUsernameTextField -> {
                viewStateInternal.update {
                    it.copy(
                        username = signUpAction.username,
                        errorMessage = null
                    )
                }
            }

            is SignUpAction.UpdateEmailTextField -> {
                viewStateInternal.update {
                    it.copy(
                        email = signUpAction.email,
                        errorMessage = null
                    )
                }
            }

            is SignUpAction.UpdatePasswordTextField -> {
                viewStateInternal.update {
                    it.copy(
                        password = signUpAction.password,
                        errorMessage = null
                    )
                }
            }

            is SignUpAction.CreateAccount -> createAccount(
                signUpAction.username,
                signUpAction.email,
                signUpAction.password
            )

            SignUpAction.OnSignUpSuccess -> {
                viewStateInternal.update {
                    it.copy(
                        hasRegistered = true,
                        errorMessage = null
                    )
                }
            }

            is SignUpAction.OnSignUpFailure -> {
                viewStateInternal.update {
                    it.copy(errorMessage = signUpAction.errorMessage)
                }
            }
        }
    }

    /**
     * Creates a new account with the given credentials in Firebase if [validateFields] is passed.
     * Stores the user data in Firestore via [storeAccountDataToFirestore].
     * If the user account couldn't be created, it will launch a [SignUpAction.OnSignUpFailure] action.
     * @param username given username
     * @param email given user email
     * @param password given user password
     */
    private fun createAccount(username: String, email: String, password: String) {
        val _username = username.trim()
        val _email = email.trim()
        val _password = password.trim()

        if (validateFields(_username, _email, _password)) { // if given data is valid
            repository.createUserWithEmailAndPassword(_email, _password) { isSuccess, errorMessage -> // initiate user account creation
                if (isSuccess) { // if the user account has been created successfully
                    repository.getCurrentUser { user ->
                        user?.let { storeAccountDataToFirestore(User(it.uid, _username, _email)) } // store user data in Firestore
                    }
                } else {
                    errorMessage?.let { handleSignUpAction(SignUpAction.OnSignUpFailure(errorMessage = it)) }
                }
            }
        }
    }

    /**
     * Stores the given [user] data into Firestore. If storing succeeds, it will launch a
     * [SignUpAction.OnSignUpSuccess] action, [SignUpAction.OnSignUpFailure] otherwise.
     * @param user data to store
     */
    private fun storeAccountDataToFirestore(user: User) {
        repository.storeAccountDataToFirestore(user) { isSuccess, errorMessage ->
            if (isSuccess) {
                handleSignUpAction(SignUpAction.OnSignUpSuccess)
            } else {
                errorMessage?.let { handleSignUpAction(SignUpAction.OnSignUpFailure(errorMessage = it)) }
            }
        }
    }

    /**
     * Checks if the given data for registration is valid. If not, it will launch [SignUpAction.OnSignUpFailure].
     * @param username given username
     * @param email given email
     * @param password given password
     * @return `true` if valid, `false` otherwise
     */
    private fun validateFields(username: String, email: String, password: String): Boolean {
        when {
            // username
            username.isEmpty() -> {
                handleSignUpAction(SignUpAction.OnSignUpFailure(errorMessage = "Username can't be empty"))
                return false
            }

            username.length < 3 -> {
                handleSignUpAction(SignUpAction.OnSignUpFailure(errorMessage = "Username is too short (min 3 characters)"))
                return false
            }

            // email
            email.isEmpty() -> {
                handleSignUpAction(SignUpAction.OnSignUpFailure(errorMessage = "Email can't be empty"))
                return false
            }

            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                handleSignUpAction(SignUpAction.OnSignUpFailure(errorMessage = "Email is invalid"))
                return false
            }

            // password
            password.isEmpty() -> {
                handleSignUpAction(SignUpAction.OnSignUpFailure(errorMessage = "Password can't be empty"))
                return false
            }

            password.length < 6 -> {
                handleSignUpAction(SignUpAction.OnSignUpFailure(errorMessage = "Password is too short (min 6 characters)"))
                return false
            }
        }

        return true
    }
}