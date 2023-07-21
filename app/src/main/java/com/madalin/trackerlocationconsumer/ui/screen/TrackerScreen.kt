package com.madalin.trackerlocationconsumer.ui.screen

import android.Manifest
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ExitToApp
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.madalin.trackerlocationconsumer.R
import com.madalin.trackerlocationconsumer.feature.auth.login.LoginAction
import com.madalin.trackerlocationconsumer.feature.tracker.TrackerAction
import com.madalin.trackerlocationconsumer.feature.tracker.TrackerViewModel
import com.madalin.trackerlocationconsumer.model.TrackingTarget
import com.madalin.trackerlocationconsumer.ui.component.RoundedIconButton
import com.madalin.trackerlocationconsumer.ui.screen.destinations.LoginScreenDestination
import com.madalin.trackerlocationconsumer.ui.theme.DarkRed
import com.madalin.trackerlocationconsumer.ui.theme.FadeGray
import com.madalin.trackerlocationconsumer.ui.theme.PastelGreen
import com.madalin.trackerlocationconsumer.ui.theme.Purple40
import com.madalin.trackerlocationconsumer.ui.theme.TintedYellow
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Destination
@Composable
fun TrackerScreen(
    trackerViewModel: TrackerViewModel = getViewModel(),
    navigator: DestinationsNavigator
) {
    val viewState by trackerViewModel.viewState.collectAsState()
    val zoomValue by remember { mutableFloatStateOf(5f) }

    val context = LocalContext.current.applicationContext
    val permissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION) // location permission state

    Box(modifier = Modifier.fillMaxSize()) {
        TrackerMap(
            //cameraPosition = viewState.cameraPosition,
            selfPosition = viewState.selfPosition,
            targetsList = viewState.targetsList,
            selectedTargetPathId = viewState.selectedTargetPathId,
            isShowSelfLocation = viewState.isShowSelfLocation,
            isShowTargetPath = viewState.isShowTargetPath,
            zoomValue = zoomValue //viewState.zoomValue,
        )

        ButtonsColumn(
            isShowSelfLocation = viewState.isShowSelfLocation,
            permissionState = permissionState,
            startShowingSelfLocation = { trackerViewModel.handleTrackerAction(TrackerAction.StartShowingSelfLocation(context)) },
            stopShowingSelfLocation = { trackerViewModel.handleTrackerAction(TrackerAction.StopShowingSelfLocation) },
            onAddTargetClick = { trackerViewModel.handleTrackerAction(TrackerAction.ToggleAddTargetDialog(true)) },
            onShowTargetsClick = { trackerViewModel.handleTrackerAction(TrackerAction.ToggleShowTargetsDialog(true)) },
            onLogOutClick = {
                trackerViewModel.handleApplicationAction(LoginAction.DoLogout)
                navigator.navigate(LoginScreenDestination)
            }
        )

        AddTargetDialog(
            isAddTargetDialogShown = viewState.isAddTargetDialogShown, // show "Add target" dialog when true
            title = stringResource(R.string.add_target_id),
            onConfirm = { targetId -> // add target by ID and hide dialog
                trackerViewModel.handleTrackerAction(TrackerAction.AddTarget(targetId))
                trackerViewModel.handleTrackerAction(TrackerAction.ToggleAddTargetDialog(false))
            },
            onCancel = { trackerViewModel.handleTrackerAction(TrackerAction.ToggleAddTargetDialog(false)) }
        )

        TargetsDialog(
            isTargetsDialogShown = viewState.isTargetsDialogShown, // show "Targets" dialog when true
            targetsList = viewState.targetsList,
            permissionState = permissionState,
            isShowTargetPath = viewState.isShowTargetPath,
            onShowRouteToTargetClick = { targetCoordinates -> trackerViewModel.handleTrackerAction(TrackerAction.ShowRouteToTarget(context, targetCoordinates)) },
            showTargetPath = { targetId -> trackerViewModel.handleTrackerAction(TrackerAction.ShowTargetPath(targetId)) },
            hideTargetPath = { trackerViewModel.handleTrackerAction(TrackerAction.HideTargetPath) },
            onDeleteTargetClick = { targetId -> trackerViewModel.handleTrackerAction(TrackerAction.DeleteTarget(targetId)) },
            onClose = { trackerViewModel.handleTrackerAction(TrackerAction.ToggleShowTargetsDialog(false)) },
        )

        /*if (permissionState.permission == Permission) {
            // Permission denied, show message and redirect to settings app on click
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri: Uri = Uri.fromParts("package", context.packageName, null)
            intent.data = uri

            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()

            TextButton(onClick = { context.startActivity(intent) }) {
                Text("Go to Settings")
            }
        }*/

        // start/stop tracking switch
        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            ToggleTrackingSwitch(
                isTracking = viewState.isTracking,
                startTracking = { trackerViewModel.handleTrackerAction(TrackerAction.StartTrackingTargets) },
                stopTracking = { trackerViewModel.handleTrackerAction(TrackerAction.StopTrackingTargets) }
            )
        }
    }
}

@Composable
fun TrackerMap(
    //cameraPosition: LatLng,
    selfPosition: LatLng,
    targetsList: List<TrackingTarget>,
    selectedTargetPathId: String,
    isShowSelfLocation: Boolean,
    isShowTargetPath: Boolean,
    zoomValue: Float,
) {
    val uiSettings by remember { mutableStateOf(MapUiSettings()) }
    val properties by remember { mutableStateOf(MapProperties(mapType = MapType.NORMAL)) }

    GoogleMap(
        //modifier = Modifier.matchParentSize(),
        //cameraPositionState = CameraPositionState(CameraPosition.fromLatLngZoom(cameraPosition, zoomValue)),
        properties = properties,
        uiSettings = uiSettings
    ) {
        // if "isShowSelfLocation" is on it shows the self marker
        if (isShowSelfLocation) {
            Marker(
                state = MarkerState(position = selfPosition),
                title = stringResource(R.string.my_location),
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)
            )
        }

        // show every target marker
        for (target in targetsList) {
            target.currentPosition?.let {
                Marker(
                    state = MarkerState(position = it),
                    title = target.id,
                    snippet = stringResource(R.string.youre_tracking_this)
                )
            }
        }

        // if "isShowTargetPath" is enabled
        if (isShowTargetPath) {
            val targetPathIndex = targetsList.indexOfFirst { it.id == selectedTargetPathId }

            Polyline(
                points = targetsList[targetPathIndex].path,
                color = Color.Blue
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ButtonsColumn(
    isShowSelfLocation: Boolean,
    permissionState: PermissionState,
    startShowingSelfLocation: () -> Unit,
    stopShowingSelfLocation: () -> Unit,
    onAddTargetClick: () -> Unit,
    onShowTargetsClick: () -> Unit,
    onLogOutClick: () -> Unit
) {
    Column(
        modifier = Modifier.padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // show self location button
        RoundedIconButton(
            icon = if (isShowSelfLocation) Icons.Rounded.Close else Icons.Rounded.LocationOn,
            onClick = {
                if (isShowSelfLocation) { // if already shown, shops showing self location
                    stopShowingSelfLocation()
                } else {
                    if (permissionState.status == PermissionStatus.Granted) { // if permission is granted
                        startShowingSelfLocation()
                    } else { // request permission
                        permissionState.launchPermissionRequest()
                    }
                }
            },
            backgroundColor = Purple40,
            contentDescription = stringResource(R.string.show_self_location)
        )

        // add target button
        RoundedIconButton(
            icon = Icons.Rounded.Add,
            onClick = { onAddTargetClick() },
            backgroundColor = Purple40,
            contentDescription = stringResource(R.string.add_target)
        )

        // show targets button
        RoundedIconButton(
            icon = Icons.Rounded.Person,
            onClick = { onShowTargetsClick() },
            backgroundColor = Purple40,
            contentDescription = stringResource(R.string.show_targets)
        )

        // logout button
        RoundedIconButton(
            icon = Icons.Rounded.ExitToApp,
            onClick = { onLogOutClick() },
            backgroundColor = Purple40,
            contentDescription = stringResource(R.string.show_targets)
        )
    }
}

@Composable
fun AddTargetDialog(
    isAddTargetDialogShown: Boolean,
    title: String,
    onConfirm: (String) -> Unit,
    onCancel: () -> Unit
) {
    if (isAddTargetDialogShown) {
        var textFieldValue by remember { mutableStateOf(TextFieldValue()) }

        AlertDialog(
            onDismissRequest = onCancel, // hide dialog when clicked outside
            title = { Text(text = title) },
            text = {
                TextField(
                    value = textFieldValue,
                    onValueChange = { textFieldValue = it },
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 17.sp)
                )
            },
            confirmButton = {
                Button(onClick = { onConfirm(textFieldValue.text) })
                { Text(text = stringResource(R.string.add_target)) }
            },
            dismissButton = {
                Button(onClick = onCancel)
                { Text(text = stringResource(R.string.cancel)) }
            }
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun TargetsDialog(
    isTargetsDialogShown: Boolean,
    targetsList: List<TrackingTarget>,
    permissionState: PermissionState,
    isShowTargetPath: Boolean,
    onShowRouteToTargetClick: (LatLng?) -> Unit,
    showTargetPath: (String) -> Unit,
    hideTargetPath: () -> Unit,
    onDeleteTargetClick: (String) -> Unit,
    onClose: () -> Unit
) {
    if (isTargetsDialogShown) {
        Dialog(onDismissRequest = { onClose() }) {
            Surface(shape = MaterialTheme.shapes.medium) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        modifier = Modifier.padding(vertical = 10.dp),
                        text = stringResource(R.string.tracking_targets),
                        fontSize = 25.sp
                    )
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(0.dp, 250.dp),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        items(targetsList) { target ->
                            TargetItem(
                                target = target,
                                permissionState = permissionState,
                                isShowTargetPath = isShowTargetPath,
                                onShowRouteToTargetClick = { onShowRouteToTargetClick(target.currentPosition) },
                                showTargetPath = { showTargetPath(target.id) },
                                hideTargetPath = { hideTargetPath() },
                                onDeleteTargetClick = { onDeleteTargetClick(target.id) }
                            )
                        }
                    }
                    Button(
                        onClick = onClose,
                        modifier = Modifier.padding(vertical = 10.dp)
                    ) {
                        Text(text = stringResource(R.string.close))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun TargetItem(
    target: TrackingTarget,
    permissionState: PermissionState,
    isShowTargetPath: Boolean,
    onShowRouteToTargetClick: (LatLng?) -> Unit,
    showTargetPath: (String) -> Unit,
    hideTargetPath: () -> Unit,
    onDeleteTargetClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = FadeGray,
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = target.id,
                    fontSize = 17.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // route to target button
            RoundedIconButton(
                icon = Icons.Rounded.Send,
                onClick = {
                    if (permissionState.status == PermissionStatus.Granted) { // if permission is granted
                        target.currentPosition?.let { onShowRouteToTargetClick(it) } // starts routing
                    } else { // request permission
                        permissionState.launchPermissionRequest()
                    }
                },
                size = 35.dp,
                backgroundColor = if (target.currentPosition != null) PastelGreen else Color.Gray
            )

            // show target path button
            RoundedIconButton(
                icon = Icons.Rounded.Search,
                onClick = {
                    if (isShowTargetPath) hideTargetPath() // if already showing, switch to hide
                    else showTargetPath(target.id)
                },
                size = 35.dp,
                backgroundColor = if (isShowTargetPath) DarkRed else TintedYellow
            )

            // delete target button
            RoundedIconButton(
                icon = Icons.Rounded.Close,
                onClick = { onDeleteTargetClick() },
                size = 35.dp,
                backgroundColor = DarkRed
            )
        }
    }
}

@Composable
fun ToggleTrackingSwitch(
    isTracking: Boolean,
    startTracking: () -> Unit,
    stopTracking: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isTracking) stringResource(R.string.tracking_is_on)
            else stringResource(R.string.tracking_is_off)
        )

        Switch(
            checked = isTracking, //uiSettings.zoomControlsEnabled,
            onCheckedChange = {
                if (isTracking) stopTracking() else startTracking() //uiSettings = uiSettings.copy(zoomControlsEnabled = it)
            }
        )
    }
}