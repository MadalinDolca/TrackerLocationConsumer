package com.madalin.trackerlocationconsumer.hivemq

data class MqttMessage(
    val clientId: String,
    val latitude: Double,
    val longitude: Double
)

