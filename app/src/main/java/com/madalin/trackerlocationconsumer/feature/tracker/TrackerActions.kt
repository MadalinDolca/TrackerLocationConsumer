package com.madalin.trackerlocationconsumer.feature.tracker

import com.madalin.trackerlocationconsumer.entity.Action
import com.madalin.trackerlocationconsumer.entity.TargetCoordinates

sealed class TrackerAction : Action {
    data class AddTarget(
        val targetName: String,
        val mqttUrl: String,
        val username: String,
        val password: String
    ) : TrackerAction()

    data class UpdateTarget(
        val id: String,
        val targetName: String,
        val mqttUrl: String,
        val username: String,
        val password: String
    ) : TrackerAction()

    data class DeleteTarget(
        val id: String
    ) : TrackerAction()

    data class CreatePath(
        val userCoordinates: TargetCoordinates,
        val targetCoordinates: TargetCoordinates
    ) : TrackerAction()

    object StartTracking : TrackerAction()
    object StopTracking : TrackerAction()
}