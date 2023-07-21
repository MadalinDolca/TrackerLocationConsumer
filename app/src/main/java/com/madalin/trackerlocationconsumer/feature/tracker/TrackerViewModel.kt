package com.madalin.trackerlocationconsumer.feature.tracker

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Looper
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.madalin.trackerlocationconsumer.entity.Action
import com.madalin.trackerlocationconsumer.hivemq.BrokerCredentials
import com.madalin.trackerlocationconsumer.hivemq.ClientCredentials
import com.madalin.trackerlocationconsumer.hivemq.Topic
import com.madalin.trackerlocationconsumer.hivemq.TrackerMqttClient
import com.madalin.trackerlocationconsumer.model.AppStateDriver
import com.madalin.trackerlocationconsumer.model.TrackingTarget
import com.madalin.trackerlocationconsumer.repository.FirebaseRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class TrackerViewModel(
    private val stateDriver: AppStateDriver, // constructor injection
    private val repository: FirebaseRepositoryImpl
) : ViewModel() {
    private val viewStateInternal = MutableStateFlow(TrackerViewState())
    val viewState = viewStateInternal.asStateFlow()

    // creates the MQTT client
    private val mqttClient = TrackerMqttClient(BrokerCredentials.host, BrokerCredentials.port, ClientCredentials.clientId)

    // location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null

    // mutual exclusion for coroutines (locked and unlocked states)
    private val stateMutex = Mutex()

    init {
        getTargetsFromFirestore()
        startTrackingTargets() // start tracking by default
    }

    /**
     * Handles the given [Action] via [AppStateDriver.handleAction].
     * @param action action to handle
     */
    fun handleApplicationAction(action: Action) {
        stateDriver.handleAction(action)
    }

    /**
     * Determines the [TrackerAction] type and calls the appropriate handle method with this
     * [trackerAction] as a parameter.
     * @param trackerAction action to handle
     */
    fun handleTrackerAction(trackerAction: TrackerAction) {
        viewModelScope.launch {
            stateMutex.withLock {
                when (trackerAction) {
                    TrackerAction.StartTrackingTargets -> startTrackingTargets()
                    TrackerAction.StopTrackingTargets -> stopTrackingTargets()
                    is TrackerAction.StartShowingSelfLocation -> startUpdateSelfPosition(trackerAction.context)
                    TrackerAction.StopShowingSelfLocation -> stopUpdateSelfPosition()

                    is TrackerAction.ToggleAddTargetDialog -> viewStateInternal.update {
                        it.copy(isAddTargetDialogShown = trackerAction.isAddTargetDialogShown)
                    }

                    is TrackerAction.ToggleShowTargetsDialog -> viewStateInternal.update {
                        it.copy(isTargetsDialogShown = trackerAction.isTargetsDialogShown)
                    }

                    is TrackerAction.AddTarget -> addTarget(trackerAction.targetId)
                    is TrackerAction.DeleteTarget -> deleteTarget(trackerAction.targetId)
                    is TrackerAction.ShowRouteToTarget -> showRouteToTarget(trackerAction.context, trackerAction.targetCoordinates)

                    is TrackerAction.ShowTargetPath -> viewStateInternal.update {
                        it.copy(
                            selectedTargetPathId = trackerAction.targetId,
                            isShowTargetPath = true
                        )
                    }

                    TrackerAction.HideTargetPath -> viewStateInternal.update {
                        it.copy(
                            selectedTargetPathId = "",
                            isShowTargetPath = false
                        )
                    }
                }
            }
        }
    }

    /**
     * Adds a new [TrackingTarget] to the state list and store it to Firestore if it doesn't
     * already exist.
     * @param targetId the target's ID to add
     */
    private fun addTarget(targetId: String) {
        val addIndex = viewStateInternal.value.targetsList.indexOfFirst { it.id == targetId }

        // if it hasn't been already added
        if (addIndex == -1) {
            val newTrackingTarget = TrackingTarget(targetId.trim())

            viewStateInternal.update { currentState ->
                val updatedTargetsList = currentState.targetsList.toMutableList()
                updatedTargetsList.add(newTrackingTarget)
                currentState.copy(targetsList = updatedTargetsList) // sets the new list
            }

            // saves the target into Firestore
            repository.getCurrentUser { user ->
                if (user != null) {
                    repository.addTarget(user.uid, newTrackingTarget) { isSuccess, errorMessage ->
                        if (isSuccess) Log.d("TrackerViewModel.addTarget()", "Target added to Firestore")
                        else Log.e("TrackerViewModel.addTarget()", errorMessage.toString())
                    }
                } else {
                    Log.e("TrackerViewModel.addTarget()", "No Firebase user available")
                }
            }

            Log.d("TrackerViewModel.addTarget()", "Added target: $targetId | List is: ${viewStateInternal.value.targetsList.toList()}")
        }
    }

    /**
     * Finds the target that has the given [targetId] and removes it from the state and from the
     * Firestore database.
     */
    private fun deleteTarget(targetId: String) {
        viewStateInternal.update { currentState ->
            val newTargetsList = currentState.targetsList.toMutableList()
            val deletionIndex = newTargetsList.indexOfFirst { it.id == targetId }

            if (deletionIndex != -1) {
                newTargetsList.removeAt(deletionIndex)
            }

            currentState.copy(targetsList = newTargetsList)
        }

        repository.getCurrentUser { user ->
            if (user != null) {
                repository.deleteTarget(user.uid, targetId) { isSuccess, errorMessage ->
                    if (isSuccess) Log.d("TrackerViewModel.deleteTarget()", "Target deleted from Firestore")
                    else Log.e("TrackerViewModel.deleteTarget()", errorMessage.toString())
                }
            } else {
                Log.e("TrackerViewModel.deleteTarget()", "No Firebase user available")
            }
        }
    }

    /**
     * Queries the Firestore database for user's targets via [repository] and adds the result to
     * the state.
     */
    private fun getTargetsFromFirestore() {
        repository.getCurrentUser { user ->
            if (user != null) {
                repository.getTargets(
                    user.uid,
                    onSuccess = { resultsList ->
                        viewStateInternal.update { it.copy(targetsList = resultsList) }
                        Log.d("TrackerViewModel.getTargetsFromFirestore()", "Got the targets from Firestore")
                    },
                    onFailure = { Log.e("TrackerViewModel.getTargetsFromFirestore()", it) })
            }
        }
    }

    /**
     * Enables the tracking state, connects to the broker, subscribes to [Topic.tracker_location],
     * receives the messages, filters them by the tracked targets IDs and updates the targets via
     * [updateTargetPosition].
     */
    private fun startTrackingTargets() {
        viewStateInternal.update { it.copy(isTracking = true) }

        // connects this MQTT client to the broker with the given credentials
        mqttClient.connectToBroker(ClientCredentials.username, ClientCredentials.password)

        if (mqttClient.isConnected()) {
            mqttClient.subscribeToTopic(Topic.tracker_location)

            // receives the messages and filters them by the tracked targets
            mqttClient.setMessageReceivedCallback { mqttMessage ->
                val trackedTargetId = viewStateInternal.value.targetsList.indexOfFirst {
                    it.id == mqttMessage.clientId
                }

                if (trackedTargetId != -1) {
                    Log.d("TrackerViewModel", "Received: $mqttMessage")
                    updateTargetPosition(mqttMessage.clientId, mqttMessage.latitude, mqttMessage.longitude)
                }
            }
        }
    }

    /**
     * Updates the current position on path of the  target with the given [targetId].
     * @param targetId the ID of the target to update
     * @param latitude new latitude coordinate
     * @param longitude new longitude coordinate
     */
    private fun updateTargetPosition(targetId: String, latitude: Double, longitude: Double) {
        viewStateInternal.update { currentViewState ->
            val updatedTargetsList = currentViewState.targetsList.toMutableList()
            val updationIndex = updatedTargetsList.indexOfFirst { it.id == targetId }

            updatedTargetsList[updationIndex].apply {
                currentPosition = LatLng(latitude, longitude)
                path.add(LatLng(latitude, longitude))
            }

            Log.d("TrackerViewModel.updateSelectedTargetPosition()", "Update: ${updatedTargetsList[updationIndex]}")
            currentViewState.copy(targetsList = updatedTargetsList, isTargetUpdate = !currentViewState.isTargetUpdate)
        }
    }

    /**
     * Changes the tracking state to `false` and disconnects the [mqttClient] from the broker.
     */
    private fun stopTrackingTargets() {
        viewStateInternal.update { it.copy(isTracking = false) }

        if (mqttClient.isConnected()) {
            mqttClient.disconnect()
        }
    }

    /**
     * Starts receiving the current location of this user once by setting a [locationCallback] to
     * update the `selfPosition` state and by calling [startLocationUpdates]. Calls
     * [openGoogleMapsForRoute] with the received user coordinates and given [targetCoordinates]
     * to show a route to the target on Google Maps.
     * @param context to pass to [openGoogleMapsForRoute]
     * @param targetCoordinates target coordinates
     */
    private fun showRouteToTarget(context: Context, targetCoordinates: LatLng?) {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { userCoordinates ->
                    if (targetCoordinates != null) {
                        val origin = "${userCoordinates.latitude},${userCoordinates.longitude}"
                        val destination = "${targetCoordinates.latitude},${targetCoordinates.longitude}"

                        viewStateInternal.update { it.copy(selfPosition = LatLng(userCoordinates.latitude, userCoordinates.longitude)) }
                        stopLocationUpdates() // get the location only one
                        locationCallback = null
                        openGoogleMapsForRoute(origin, destination, context)
                    } else {
                        Log.e("TrackerViewModel.showRouteToTarget()", "Target coordinates are null")
                    }
                }
            }
        }

        startLocationUpdates(context)
    }

    /**
     * Starts receiving the user location via [startLocationUpdates] and updates the states of
     * `isShowSelfLocation` and `selfPosition`.
     * @param context to pass to [startLocationUpdates]
     */
    private fun startUpdateSelfPosition(context: Context) {
        viewStateInternal.update { it.copy(isShowSelfLocation = true) }

        // behavior to handle the received location updates
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) { // most recent location information
                locationResult.lastLocation?.let { location ->
                    viewStateInternal.update { it.copy(selfPosition = LatLng(location.latitude, location.longitude)) }
                    Log.d("TrackerViewModel.startUpdateSelfPosition()", "Self coordinates: ${location.latitude}, ${location.longitude}")
                }
            }
        }

        startLocationUpdates(context)
    }

    /**
     * Changes the state of `isShowSelfLocation` to false and calls [stopLocationUpdates] to
     * stop the updates.
     */
    private fun stopUpdateSelfPosition() {
        viewStateInternal.update { it.copy(isShowSelfLocation = false) }
        stopLocationUpdates()
    }

    /**
     * Starts requesting location updates based on [LocationRequest] every 5 seconds.
     * @param context context to create the [FusedLocationProviderClient]
     */
    @SuppressLint("MissingPermission")
    private fun startLocationUpdates(context: Context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L).build()
        locationCallback?.let { fusedLocationClient.requestLocationUpdates(locationRequest, it, Looper.getMainLooper()) }
    }

    /**
     * Removes all location updates of [fusedLocationClient] that have [locationCallback] as a callback.
     */
    private fun stopLocationUpdates() {
        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
    }

    /**
     * Opens the Google Maps app if available and provides it a [source] and a [destination] to show
     * a route between them.
     * @param source start location
     * @param destination end location
     * @param context global Application object of the current process
     */
    private fun openGoogleMapsForRoute(source: String, destination: String, context: Context) {
        try {
            val mapsUri = Uri.parse("https://www.google.com/maps/dir/?api=1&origin=$source&destination=$destination&travelmode=driving")
            val openMapsIntent = Intent(Intent.ACTION_VIEW, mapsUri)

            openMapsIntent.setPackage("com.google.android.apps.maps") // maps package name
            openMapsIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

            context.startActivity(openMapsIntent)
        } catch (e: ActivityNotFoundException) {
            // when Google Maps is not installed on the device it will open Google Play to download it
            val playStoreUri = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.maps")
            val openPlayStoreIntent = Intent(Intent.ACTION_VIEW, playStoreUri) // initializing intent with action view

            openPlayStoreIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

            context.startActivity(openPlayStoreIntent)
        }
    }
}