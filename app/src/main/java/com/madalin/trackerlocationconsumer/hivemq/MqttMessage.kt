package com.madalin.trackerlocationconsumer.hivemq

data class MqttMessage(
    val topic: String,
    val message: String
)

