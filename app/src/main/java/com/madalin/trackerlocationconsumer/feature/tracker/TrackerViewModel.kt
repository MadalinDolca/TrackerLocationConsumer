package com.madalin.trackerlocationconsumer.feature.tracker

import androidx.lifecycle.ViewModel
import com.madalin.trackerlocationconsumer.model.AppStateDriver
import com.madalin.trackerlocationconsumer.hivemq.BrokerCredentials
import com.madalin.trackerlocationconsumer.hivemq.ClientCredentials
import com.madalin.trackerlocationconsumer.hivemq.Topic
import com.madalin.trackerlocationconsumer.hivemq.TrackerMqttClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TrackerViewModel(
    private val stateDriver: AppStateDriver // constructor injection
) : ViewModel() {
    private val viewStateInternal = MutableStateFlow(TrackerViewState())
    val viewState = viewStateInternal.asStateFlow()

    private val mqttClient = TrackerMqttClient(BrokerCredentials.host, BrokerCredentials.port)

    init {
        // connects this MQTT client to the broker with the given credentials
        mqttClient.connectToBroker(ClientCredentials.username, ClientCredentials.password)

        if (mqttClient.isConnected()) {
            mqttClient.subscribeToTopic(Topic.tracker_location)

            mqttClient.setMessageReceivedCallback { mqttMessage ->
                viewStateInternal.update { it.copy(coordinates = mqttMessage.message) }
            }
        }
    }
}