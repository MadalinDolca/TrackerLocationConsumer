package com.madalin.trackerlocationconsumer.feature.tracker

import android.content.Context
import com.madalin.trackerlocationconsumer.entity.Action

sealed class TrackerAction : Action {
    data class ToggleAddTargetDialog(
        val isAddTargetDialogShown: Boolean
    ) : TrackerAction()

    data class ToggleShowTargetsDialog(
        val isTargetsDialogShown: Boolean
    ) : TrackerAction()

    data class AddTarget(
        val targetId: String
    ) : TrackerAction()

    data class DeleteTarget(
        val targetId: String
    ) : TrackerAction()

    data class CreatePath(
        val targetId: String
    ) : TrackerAction()

    object StartTrackingTargets : TrackerAction()
    object StopTrackingTargets : TrackerAction()

    data class StartBringToTarget(
        val context: Context
    ) : TrackerAction()

    object StopBringToTarget : TrackerAction()
}