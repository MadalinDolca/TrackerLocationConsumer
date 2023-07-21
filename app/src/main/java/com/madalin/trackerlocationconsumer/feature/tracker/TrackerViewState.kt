package com.madalin.trackerlocationconsumer.feature.tracker

import com.google.android.gms.maps.model.LatLng
import com.madalin.trackerlocationconsumer.model.TrackingTarget

data class TrackerViewState(
    val isTracking: Boolean = false,
    val isShowSelfLocation: Boolean = false,
    val cameraPosition: LatLng = LatLng(47.656203, 23.571124),
    val selfPosition: LatLng = LatLng(0.0, 0.0),
    val targetsList: List<TrackingTarget> = emptyList(),
    val selectedTargetPathId: String = "",
    val isShowTargetPath: Boolean = false,
    val isTargetUpdate: Boolean = false,
    val zoomValue: Float = 15f,
    val isAddTargetDialogShown: Boolean = false,
    val isTargetsDialogShown: Boolean = false
)