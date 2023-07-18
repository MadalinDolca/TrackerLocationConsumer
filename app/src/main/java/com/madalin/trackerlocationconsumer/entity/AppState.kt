package com.madalin.trackerlocationconsumer.entity

/**
 * The overall state of the application. It serves as a centralized container for storing and
 * managing different aspects of the application state.
 */
data class AppState(
    val loginState: AppLoginState = AppLoginState(), // user's login state
    val trackingState: List<TrackingTarget> = emptyList(), // user's list of tracking targets
    val coordinatesState: List<TargetCoordinates> = emptyList() // user's list of target coordinates
)
