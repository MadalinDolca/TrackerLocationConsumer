package com.madalin.trackerlocationconsumer.feature.tracker

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.madalin.trackerlocationconsumer.model.AppStateDriver
import com.madalin.trackerlocationconsumer.hivemq.BrokerCredentials
import com.madalin.trackerlocationconsumer.hivemq.ClientCredentials
import com.madalin.trackerlocationconsumer.hivemq.MqttMessage
import com.madalin.trackerlocationconsumer.hivemq.Topic
import com.madalin.trackerlocationconsumer.hivemq.TrackerMqttClient
import com.madalin.trackerlocationconsumer.util.convertStringToLatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TrackerViewModel(
    private val stateDriver: AppStateDriver // constructor injection
) : ViewModel() {
    private val viewStateInternal = MutableStateFlow(TrackerViewState())
    val viewState = viewStateInternal.asStateFlow()

    // creates the MQTT client
    private val mqttClient = TrackerMqttClient(BrokerCredentials.host, BrokerCredentials.port, ClientCredentials.clientId)
    private val gson = Gson()

    init {

    }

    fun handleTrackerAction(trackerAction: TrackerAction) {
        when (trackerAction) {
            is TrackerAction.AddTarget -> TODO()
            is TrackerAction.DeleteTarget -> TODO()
            is TrackerAction.UpdateTarget -> TODO()
            is TrackerAction.CreatePath -> TODO()

            TrackerAction.StartTracking -> startTracking()
            TrackerAction.StopTracking -> stopTracking()
            TrackerAction.StartBringToTarget -> startBringToTarget()
            TrackerAction.StopBringToTarget -> stopBringToTarget()
        }
    }

    /**
     * Enables the tracking state, connects to the broker, subscribes to [Topic.tracker_location]
     * and receives the messages.
     */
    private fun startTracking() {
        viewStateInternal.update { it.copy(isTracking = true) }

        // connects this MQTT client to the broker with the given credentials
        mqttClient.connectToBroker(ClientCredentials.username, ClientCredentials.password)

        if (mqttClient.isConnected()) {
            mqttClient.subscribeToTopic(Topic.tracker_location)

            mqttClient.setMessageReceivedCallback { mqttMessage ->
                Log.d("TrackerViewModel", "Received: $mqttMessage")

                viewStateInternal.update { currentState ->
                    //val currentCoordinates = currentState.coordinates.toMutableList() // get the current list
                    //latLng?.let { currentCoordinates.add(it) } // append the new coordinates
                    //currentState.copy(coordinates = currentCoordinates) // create the new state

                    currentState.copy(
                        cameraPosition = LatLng(mqttMessage.latitude, mqttMessage.longitude),
                        targetPosition = LatLng(mqttMessage.latitude, mqttMessage.longitude)
                    )
                }
            }
        }
    }

    private fun stopTracking() {
        viewStateInternal.update { it.copy(isTracking = false) }

        if (mqttClient.isConnected()) {
            mqttClient.disconnect()
        }
    }

    private fun startBringToTarget() {
        viewStateInternal.update { it.copy(isBringToTargetOn = true) }
    }

    private fun stopBringToTarget() {
        viewStateInternal.update { it.copy(isBringToTargetOn = false) }
    }

    private fun requestLocationPermission() {

    }
}