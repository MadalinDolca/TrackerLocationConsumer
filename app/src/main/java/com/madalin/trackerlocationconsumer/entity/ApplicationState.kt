package com.madalin.trackerlocationconsumer.entity

import com.madalin.trackerlocationconsumer.feature.login.LoginState

/**
 * The overall state of the application. It serves as a centralized container for storing and
 * managing different aspects of the application state.
 */
data class ApplicationState(
    val loginState: LoginState = LoginState(), // user's login state
    val trackingState: List<TrackingTarget> = emptyList(), // user's list of tracking targets
    val coordinatesState: List<TargetCoordinates> = emptyList() // user's list of target coordinates
)
