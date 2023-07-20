package com.madalin.trackerlocationconsumer.feature.tracker

import com.google.android.gms.maps.model.LatLng

data class TrackerViewState(
    val isTracking: Boolean = false,
    val cameraPosition: LatLng = LatLng(0.0, 0.0),
    val targetPosition: LatLng = LatLng(0.0, 0.0),
    val selfPosition: LatLng = LatLng(0.0, 0.0),
    val isBringToTargetOn: Boolean = false,
    val zoomValue: Float = 15f
)