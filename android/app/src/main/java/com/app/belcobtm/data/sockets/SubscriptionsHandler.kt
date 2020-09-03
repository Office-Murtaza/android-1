package com.app.belcobtm.data.sockets

import java.util.*

class SubscriptionsHandler(var topic: String? = null) {
    private val listeners: MutableSet<SocketMessageListener> =
        HashSet()

    fun addListener(listener: SocketMessageListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: SocketMessageListener?) {
        listeners.remove(listener)
    }

    fun onMessage(message: SocketMessage) {
        for (listener in listeners) {
            listener.onMessage(message)
        }
    }
}