package com.madalin.trackerlocationconsumer.hivemq

object BrokerCredentials {
    const val host = "868b4e59e6c341379e82120b3e1d456e.s2.eu.hivemq.cloud" // cluster url
    const val port = 8883
    const val websocketPort = 8884

    /**
     * Broker's address that is a combination of [host] and [port].
     * Looks like this `tcp://<host>:<port>`.
     */
    const val serverAddress = "tcp://$host:$port"

    const val webSocketServerAddress = "ws://$host:$websocketPort"
}