package com.madalin.trackerlocationconsumer.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class User(
    var id: String = "",
    var email: String = "",
    var name: String = "",
    @ServerTimestamp var createdAt: Date? = null,
    @ServerTimestamp var updatedAt: Date? = null
)
