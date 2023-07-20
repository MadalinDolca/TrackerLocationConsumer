package com.madalin.trackerlocationconsumer.feature.tracker

import android.annotation.SuppressLint
import android.content.Context
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
                    is TrackerAction.ToggleAddTargetDialog -> viewStateInternal.update {
                        it.copy(isAddTargetDialogShown = trackerAction.isAddTargetDialogShown)
                    }

                    is TrackerAction.AddTarget -> addTarget(trackerAction.targetId)

                    is TrackerAction.DeleteTarget -> deleteTarget(trackerAction.targetId)

                    is TrackerAction.ToggleShowTargetsDialog -> viewStateInternal.update {
                        it.copy(isTargetsDialogShown = trackerAction.isTargetsDialogShown)
                    }

                    is TrackerAction.CreatePath -> TODO()

                    TrackerAction.StartTrackingTargets -> startTrackingTargets()
                    TrackerAction.StopTrackingTargets -> stopTrackingTargets()
                    is TrackerAction.StartBringToTarget -> startBringToTarget(trackerAction.context)
                    TrackerAction.StopBringToTarget -> stopBringToTarget()
                }
            }
        }
    }

    /**
     * Adds a new [TrackingTarget] to the state list and store it to Firestore.
     * @param targetId the target's ID to add
     */
    private fun addTarget(targetId: String) {
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
     * [updateSelectedTargetPosition].
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
                    updateSelectedTargetPosition(mqttMessage.clientId, mqttMessage.latitude, mqttMessage.longitude)
                }
            }
        }
    }

    private fun updateSelectedTargetPosition(id: String, latitude: Double, longitude: Double) {
        val currentState = viewStateInternal.value
        val updatedTargets = currentState.targetsList.toMutableList()
        val targetIndex = updatedTargets.indexOfFirst { it.id == id }

        if (targetIndex != -1) {
            updatedTargets[targetIndex].currentPosition = LatLng(latitude, longitude)
            viewStateInternal.value = currentState.copy(targetsList = updatedTargets)
            //viewStateInternal.update { it.copy(targetsList = updatedTargets) }
            Log.d("TrackerViewModel.updateSelectedTargetPosition()", "Update: ${updatedTargets[targetIndex]}")
        }

        /*viewStateInternal.update { currentState ->
            val updatedTargetsList = currentState.targetsList.toMutableList() // get the current list

            latLng?.let { updatedTargetsList.add(it) } // append the new coordinates
            currentState.copy(coordinates = updatedTargetsList) // create the new state

            currentState.copy(
                cameraPosition = LatLng(mqttMessage.latitude, mqttMessage.longitude),
                targetPosition = LatLng(mqttMessage.latitude, mqttMessage.longitude)
            )
        }*/
    }

    private fun stopTrackingTargets() {
        viewStateInternal.update { it.copy(isTracking = false) }

        if (mqttClient.isConnected()) {
            mqttClient.disconnect()
        }
    }

    private fun showTrackingTargets() {

    }

    private fun startBringToTarget(context: Context) {
        viewStateInternal.update { it.copy(isBringToTargetOn = true) }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        // behavior to handle the received location updates
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) { // most recent location information
                locationResult.lastLocation?.let { location ->
                    viewStateInternal.update { it.copy(selfPosition = LatLng(location.latitude, location.longitude)) }
                    Log.d("TrackerViewModel.startBringToTarget()", "Self coordinates: ${location.latitude}, ${location.longitude}")
                }
            }
        }

        startLocationUpdates()
    }

    private fun stopBringToTarget() {
        viewStateInternal.update { it.copy(isBringToTargetOn = false) }
        stopLocationUpdates()
    }

    /**
     * Starts requesting location updates based on [LocationRequest] every 5 seconds.
     */
    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L).build()
        locationCallback?.let { fusedLocationClient.requestLocationUpdates(locationRequest, it, Looper.getMainLooper()) }
    }

    /**
     * Removes all location updates of [fusedLocationClient] that have [locationCallback] as a callback.
     */
    private fun stopLocationUpdates() {
        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
    }
}