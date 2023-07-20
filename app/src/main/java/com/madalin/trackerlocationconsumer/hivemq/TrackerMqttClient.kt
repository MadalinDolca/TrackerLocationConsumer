package com.madalin.trackerlocationconsumer.hivemq

import android.util.Log
import com.google.gson.Gson
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.MqttClientState
import com.hivemq.client.mqtt.MqttGlobalPublishFilter
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import java.nio.charset.StandardCharsets

class TrackerMqttClient(var host: String, var port: Int, var clientId: String) {
    /**
     * Creates an MQTT client that can connect to [host] at [port].
     */
    private var client = MqttClient.builder()
        .identifier(clientId)
        .useMqttVersion5()
        .serverHost(host)
        .serverPort(port)
        .sslWithDefaultConfig()
        .buildBlocking()
    //.automaticReconnectWithDefaultConfig()

    private var gson = Gson()
    private var messageReceivedCallback: ((MqttMessage) -> Unit)? = null // stores the callback function

    /**
     * Connects to HiveMQ Cloud with TLS and username/password.
     */
    fun connectToBroker(username: String, password: String) {
        client.connectWith()
            .simpleAuth()
            .username(username)
            .password(StandardCharsets.UTF_8.encode(password))
            .applySimpleAuth()
            .send()
    }

    /**
     * Subscribes to the given topic and sets a callback that is called when a message is received.
     */
    fun subscribeToTopic(topic: String) {
        client.subscribeWith()
            .topicFilter(topic)
            .send()

        // set a callback that is called when a message is received (using the async API style)
        client.toAsync().publishes(MqttGlobalPublishFilter.ALL) { publish: Mqtt5Publish ->
            //println("Received message: " + publish.topic + " -> " + StandardCharsets.UTF_8.decode(publish.payload.get()))
            //val mqttMessage = MqttMessage(publish.topic.toString(), publish.payloadAsBytes.decodeToString())
            val jsonMessage = gson.fromJson(publish.payloadAsBytes.decodeToString(), MqttMessage::class.java)
            messageReceivedCallback?.invoke(jsonMessage) // invokes the callback function
        }
    }

    /**
     * Callback to receive the published MQTT messages.
     */
    fun setMessageReceivedCallback(callback: (MqttMessage) -> Unit) {
        messageReceivedCallback = callback
    }

    /**
     * Publishes the given message to the given topic.
     * @param topic where to publish
     * @param clientId the client that sends the message
     * @param latitude of the location
     * @param longitude of the location
     */
    fun publishToTopic(topic: String, clientId: String, latitude: Double, longitude: Double) {
        val message = MqttMessage(clientId, latitude, longitude)
        val jsonMessage = gson.toJson(message).toByteArray()

        client.publishWith()
            .topic(topic)
            .payload(jsonMessage/*StandardCharsets.UTF_8.encode(message)*/)
            .send()

        Log.d("TrackerMqttClient", "Message published: $message")
    }

    /**
     * Checks if the [client] is connected to the broker.
     * @return true if the client is connected
     */
    fun isConnected() = client.state == MqttClientState.CONNECTED

    /**
     * Disconnects this [client] with the default disconnect message.
     */
    fun disconnect() {
        client.disconnect()
    }
}