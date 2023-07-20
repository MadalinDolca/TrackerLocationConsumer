package com.madalin.trackerlocationconsumer.repository

import com.google.firebase.auth.FirebaseUser
import com.madalin.trackerlocationconsumer.model.TrackingTarget
import com.madalin.trackerlocationconsumer.model.User

interface FirebaseRepository {
    fun createUserWithEmailAndPassword(email: String, password: String, onComplete: (Boolean, String?) -> Unit)
    fun storeAccountDataToFirestore(user: User, onComplete: (Boolean, String?) -> Unit)
    fun getCurrentUser(onUserRetrieved: (FirebaseUser?) -> Unit)
    fun signInWithEmailAndPassword(email: String, password: String, onComplete: (Boolean, String?) -> Unit)
    fun signOut()
    fun addTarget(userId: String, trackingTarget: TrackingTarget, onComplete: (Boolean, String?) -> Unit)
    fun getTargets(userId: String, onSuccess: (List<TrackingTarget>) -> Unit, onFailure: (String) -> Unit)
    fun deleteTarget(userId: String, targetId: String, onComplete: (Boolean, String?) -> Unit)
}