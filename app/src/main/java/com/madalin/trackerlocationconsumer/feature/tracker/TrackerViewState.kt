package com.madalin.trackerlocationconsumer.feature.tracker

data class TrackerViewState(
    val isTracking: Boolean = false,
    val coordinates: String? = null
)