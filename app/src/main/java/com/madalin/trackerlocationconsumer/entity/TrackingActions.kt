package com.madalin.trackerlocationconsumer.entity

sealed class TrackingAction : Action {
    data class AddTarget(
        val targetName: String,
        val mqttUrl: String,
        val username: String,
        val password: String
    ) : TrackingAction()

    data class UpdateTarget(
        val id: String,
        val targetName: String,
        val mqttUrl: String,
        val username: String,
        val password: String
    ) : TrackingAction()

    data class DeleteTarget(
        val id: String
    ) : TrackingAction()
}