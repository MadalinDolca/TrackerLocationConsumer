package com.madalin.trackerlocationconsumer.repository

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.madalin.trackerlocationconsumer.model.User
import com.madalin.trackerlocationconsumer.util.DBCollection

class FirebaseRepositoryImpl : FirebaseRepository {
    private val auth = Firebase.auth
    private val firestore = Firebase.firestore

    /**
     * Creates a new user account in Firebase with the provided [email] and [password].
     * @param email the email of the user for registration
     * @param password the password of the user for registration
     * @param onComplete callback function that will be invoked once the account creation process is completed
     */
    override fun createUserWithEmailAndPassword(email: String, password: String, onComplete: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { accountCreationTask ->
                onComplete(accountCreationTask.isSuccessful, accountCreationTask.exception?.message)
            }
            .addOnFailureListener {
                onComplete(false, it.message)
            }
    }

    /**
     * Stores the given [user] data to Firestore into a collection with the same name as the user id.
     * @param user the user data to be stored in Firestore
     * @param onComplete callback function that will be invoked once the data storage process is completed
     */
    override fun storeAccountDataToFirestore(user: User, onComplete: (Boolean, String?) -> Unit) {
        firestore.collection(DBCollection.USERS)
            .document(user.id)
            .set(user)
            .addOnCompleteListener {
                onComplete(true, null)
            }
            .addOnFailureListener {
                onComplete(false, it.message)
            }
    }

    /**
     * Retrieves the currently logged-in user from Firebase Authentication.
     * @param onUserRetrieved Callback function that will be invoked once the user is retrieved.
     *                        The function will receive the currently logged-in user as a parameter.
     */
    override fun getCurrentUser(onUserRetrieved: (FirebaseUser?) -> Unit) {
        val currentUser = auth.currentUser
        onUserRetrieved(currentUser)
    }

    /**
     * Signs in the user with the given [email] and [password].
     * @param email given email
     * @param password given password
     * @param onComplete callback function that will be invoked once the authentication process is completed
     */
    override fun signInWithEmailAndPassword(email: String, password: String, onComplete: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                onComplete(it.isSuccessful, null)
            }
            .addOnFailureListener {
                onComplete(false, it.message.toString())
            }
    }

    /**
     * Signs out the current user.
     */
    override fun signOut() {
        auth.signOut()
    }
}