package com.madalin.trackerlocationconsumer.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.madalin.trackerlocationconsumer.R
import com.madalin.trackerlocationconsumer.feature.tracker.TrackerAction
import com.madalin.trackerlocationconsumer.feature.tracker.TrackerViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import org.koin.androidx.compose.getViewModel

@RootNavGraph(start = true)
@Destination
@Composable
fun TrackerScreen(
    trackerViewModel: TrackerViewModel = getViewModel(),
    navigator: DestinationsNavigator
) {
    val viewState by trackerViewModel.viewState.collectAsState()
    var uiSettings by remember { mutableStateOf(MapUiSettings()) }
    val properties by remember { mutableStateOf(MapProperties(mapType = MapType.NORMAL)) }

    Box(modifier = Modifier.fillMaxSize()) {
        TrackerMap(
            cameraPosition = viewState.cameraPosition,
            selfPosition = viewState.selfPosition,
            targetPosition = viewState.targetPosition,
            isBringToTargetOn = viewState.isBringToTargetOn,
            zoomValue = viewState.zoomValue,
            properties = properties,
            uiSettings = uiSettings
        )
        Switch(
            checked = uiSettings.zoomControlsEnabled,
            onCheckedChange = { uiSettings = uiSettings.copy(zoomControlsEnabled = it) }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
        ) {
            ToggleTrackingButton(
                isTracking = viewState.isTracking,
                startTracking = { trackerViewModel.handleTrackerAction(TrackerAction.StartTracking) },
                stopTracking = { trackerViewModel.handleTrackerAction(TrackerAction.StopTracking) }
            )

            // if tracking in on, it will show the "Bring to target" button
            if (viewState.isTracking) {
                BringToTargetButton(
                    isBringToTargetOn = viewState.isBringToTargetOn,
                    onStart = { trackerViewModel.handleTrackerAction(TrackerAction.StartBringToTarget) },
                    onStop = { trackerViewModel.handleTrackerAction(TrackerAction.StopBringToTarget) }
                )
            }
        }
    }
}

@Composable
fun TrackerMap(
    cameraPosition: LatLng,
    selfPosition: LatLng,
    targetPosition: LatLng,
    zoomValue: Float,
    isBringToTargetOn: Boolean,
    properties: MapProperties,
    uiSettings: MapUiSettings
) {
    GoogleMap(
        //modifier = Modifier.matchParentSize(),
        cameraPositionState = CameraPositionState(CameraPosition.fromLatLngZoom(cameraPosition, zoomValue)),
        properties = properties,
        uiSettings = uiSettings
    ) {
        // target marker
        Marker(
            state = MarkerState(position = targetPosition),
            title = "Target",
            snippet = "marker snippet"
        )

        // if "Bring to target is on" it shows the self marker and a line to the target
        if (isBringToTargetOn) {
            Marker(
                state = MarkerState(position = selfPosition),
                title = stringResource(R.string.my_location),
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)
            )
            Polyline(
                points = listOf(selfPosition, targetPosition),
                color = Color.Red
            )
        }
    }
}

@Composable
fun ToggleTrackingButton(isTracking: Boolean, startTracking: () -> Unit, stopTracking: () -> Unit) {
    Button(onClick = {
        if (isTracking) stopTracking()
        else startTracking()
    }) {
        Text(
            text = if (isTracking) stringResource(R.string.stop_tracking)
            else stringResource(R.string.start_tracking)
        )
    }
}

@Composable
fun BringToTargetButton(isBringToTargetOn: Boolean, onStart: () -> Unit, onStop: () -> Unit) {
    Button(onClick = {
        if (isBringToTargetOn) onStop()
        else onStart()
    }) {
        Text(
            text = if (isBringToTargetOn) stringResource(R.string.stop_bringing)
            else stringResource(R.string.bring_me_to_target)
        )
    }
}