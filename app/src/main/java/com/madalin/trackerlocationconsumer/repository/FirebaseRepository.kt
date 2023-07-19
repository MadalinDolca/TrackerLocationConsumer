package com.madalin.trackerlocationconsumer.repository

import com.google.firebase.auth.FirebaseUser
import com.madalin.trackerlocationconsumer.model.User

interface FirebaseRepository {
    fun createUserWithEmailAndPassword(email: String, password: String, onComplete: (Boolean, String?) -> Unit)
    fun storeAccountDataToFirestore(user: User, onComplete: (Boolean, String?) -> Unit)
    fun getCurrentUser(onUserRetrieved: (FirebaseUser?) -> Unit)
    fun signInWithEmailAndPassword(email: String, password: String, onComplete: (Boolean, String?) -> Unit)
    fun signOut()
}