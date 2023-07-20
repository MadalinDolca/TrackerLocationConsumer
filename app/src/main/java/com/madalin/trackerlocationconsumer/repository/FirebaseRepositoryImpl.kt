package com.madalin.trackerlocationconsumer.repository

import android.util.Log
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.madalin.trackerlocationconsumer.model.TrackingTarget
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

    /**
     * Adds the given [trackingTarget] in the [DBCollection.TARGETS] collection of the user with the
     * id [userId],
     * @param userId the ID of the user where to add the data
     * @param trackingTarget data to add to the user's collection
     * @param onComplete callback function that will be invoked once the adding process is completed
     */
    override fun addTarget(userId: String, trackingTarget: TrackingTarget, onComplete: (Boolean, String?) -> Unit) {
        firestore.collection(DBCollection.USERS)
            .document(userId)
            .collection(DBCollection.TARGETS)
            .document(trackingTarget.id)
            .set(trackingTarget)
            .addOnCompleteListener {
                onComplete(true, null)
            }
            .addOnFailureListener {
                onComplete(false, it.message)
            }
    }

    /**
     * Obtains the list of [TrackingTarget]s from the user that has the given [userId].
     * @param userId the ID of the user from where to get the targets
     * @param onSuccess callback function that will be invoked once the fetching process has succeed
     * @param onFailure callback function that will be invoked once the fetching process has failed
     * @return the list of tracking targets
     */
    override fun getTargets(userId: String, onSuccess: (List<TrackingTarget>) -> Unit, onFailure: (String) -> Unit) {
        val targetsList = mutableListOf<TrackingTarget>()

        firestore.collection(DBCollection.USERS)
            .document(userId)
            .collection(DBCollection.TARGETS)
            .get()
            .addOnSuccessListener { results ->
                for (document in results) {
                    val target = document.toObject<TrackingTarget>()
                    targetsList.add(target)
                }

                onSuccess(targetsList)
            }
            .addOnFailureListener {
                onFailure(it.message.toString())
            }
    }

    /**
     * Finds and deletes the target with the given [targetId] from the user's [DBCollection.TARGETS]
     * collection.
     * @param userId the ID of the user that is tracking the target
     * @param targetId the ID of the target to delete
     * @param onComplete callback function that will be invoked once the deletion process is completed
     */
    override fun deleteTarget(userId: String, targetId: String, onComplete: (Boolean, String?) -> Unit) {
        firestore.collection(DBCollection.USERS)
            .document(userId)
            .collection(DBCollection.TARGETS)
            .document(targetId)
            .delete()
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { onComplete(false, it.message) }
    }
}