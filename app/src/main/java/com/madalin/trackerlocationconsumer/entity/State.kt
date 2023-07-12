package com.madalin.trackerlocationconsumer.entity

data class State(
    val loginState: LoginState = LoginState(),
    val trackingState: List<TrackingTarget> = emptyList(),
    val coordinatesState: List<TargetCoordinates> = emptyList()
)
