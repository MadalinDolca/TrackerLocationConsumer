package com.madalin.trackerlocationconsumer.entity

data class TrackingTarget(
    val id: String,
    val name: String,
    val brokerUrl: String,
    val username: String,
    val password: String,
    val topic: String
)
