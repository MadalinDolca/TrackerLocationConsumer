package com.madalin.trackerlocationconsumer.feature.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.madalin.trackerlocationconsumer.model.User
import com.madalin.trackerlocationconsumer.util.DBCollection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SignUpViewModel : ViewModel() {
    private val viewStateInternal = MutableStateFlow(SignUpViewState())
    val viewState = viewStateInternal.asStateFlow()

    private val auth = Firebase.auth
    private val firestore = Firebase.firestore

    /**
     * Determines the [SignUpScreenAction] type and calls the appropriate handle method with this
     * [signUpAction] as a parameter.
     * @param signUpAction action to handle
     */
    fun handleAction(signUpAction: SignUpScreenAction) {
        when (signUpAction) {
            is SignUpScreenAction.UpdateUsernameTextField -> {
                viewStateInternal.update {
                    it.copy(
                        username = signUpAction.username,
                        errorMessage = null
                    )
                }
            }

            is SignUpScreenAction.UpdateEmailTextField -> {
                viewStateInternal.update {
                    it.copy(
                        email = signUpAction.email,
                        errorMessage = null
                    )
                }
            }

            is SignUpScreenAction.UpdatePasswordTextField -> {
                viewStateInternal.update {
                    it.copy(
                        password = signUpAction.password,
                        errorMessage = null
                    )
                }
            }

            is SignUpScreenAction.CreateAccount -> createAccount(
                signUpAction.username,
                signUpAction.email,
                signUpAction.password
            )

            SignUpScreenAction.OnSignUpSuccess -> {
                viewStateInternal.update {
                    it.copy(
                        hasRegistered = true,
                        errorMessage = null
                    )
                }
            }

            is SignUpScreenAction.OnSignUpFailure -> {
                viewStateInternal.update {
                    it.copy(errorMessage = signUpAction.errorMessage)
                }
            }
        }
    }

    /**
     * Creates a new account with the given credentials in Firebase if [validateFields] is passed.
     * Stores the user data in Firestore via [storeAccountDataToFirestore].
     * @param username given username
     * @param email given user email
     * @param password given user password
     */
    private fun createAccount(username: String, email: String, password: String) {
        val _username = username.trim()
        val _email = email.trim()
        val _password = password.trim()

        // if given data is valid
        if (validateFields(_username, _email, _password)) {
            // initiate user account creation
            auth.createUserWithEmailAndPassword(_email, _password)
                .addOnCompleteListener { accountCreationTask ->
                    // if the user account has been created successfully
                    if (accountCreationTask.isSuccessful) {
                        auth.currentUser?.let {
                            storeAccountDataToFirestore(User(it.uid, username, email))
                        }
                    }
                }
                .addOnFailureListener {
                    handleAction(SignUpScreenAction.OnSignUpFailure(errorMessage = it.message.toString()))
                }
        }
    }

    /**
     * Stores the given [user] data into Firestore to a collection with the same name as the user id.
     * @param user data to store
     */
    private fun storeAccountDataToFirestore(user: User) {
        firestore.collection(DBCollection.USERS)
            .document(user.id) // adds user's data into the document with the user id as the name
            .set(user)
            .addOnCompleteListener {
                handleAction(SignUpScreenAction.OnSignUpSuccess)
            }
            .addOnFailureListener {
                handleAction(SignUpScreenAction.OnSignUpFailure(errorMessage = it.message.toString()))
            }
    }

    /**
     * Checks if the given data for registration is valid.
     * @param username given username
     * @param email given email
     * @param password given password
     * @return `true` if valid, `false` otherwise
     */
    private fun validateFields(username: String, email: String, password: String): Boolean {
        when {
            // username
            username.isEmpty() -> {
                handleAction(SignUpScreenAction.OnSignUpFailure(errorMessage = "Username can't be empty"))
                return false
            }

            username.length < 3 -> {
                handleAction(SignUpScreenAction.OnSignUpFailure(errorMessage = "Username is too short (min 3 characters)"))
                return false
            }

            // email
            email.isEmpty() -> {
                handleAction(SignUpScreenAction.OnSignUpFailure(errorMessage = "Email can't be empty"))
                return false
            }

            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                handleAction(SignUpScreenAction.OnSignUpFailure(errorMessage = "Email is invalid"))
                return false
            }

            // password
            password.isEmpty() -> {
                handleAction(SignUpScreenAction.OnSignUpFailure(errorMessage = "Password can't be empty"))
                return false
            }

            password.length < 6 -> {
                handleAction(SignUpScreenAction.OnSignUpFailure(errorMessage = "Password is too short (min 6 characters)"))
                return false
            }
        }

        return true
    }
}